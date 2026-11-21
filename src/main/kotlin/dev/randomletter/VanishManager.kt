package dev.randomletter

import org.bukkit.Bukkit
import org.bukkit.ChunkSnapshot
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.block.data.BlockData
import org.bukkit.scheduler.BukkitTask
import kotlin.math.floor

/**
 * Entfernt alle Blöcke eines Buchstabens in den Chunks der Spieler und stellt sie
 * nach Ablauf der Zeit wieder her. Gescannt wird asynchron über ChunkSnapshots,
 * entfernt/wiederhergestellt wird gestaffelt auf dem Main-Thread.
 */
class VanishManager(private val plugin: RandomLetterPlugin) {

    private class ChunkScan(val world: World, val chunkX: Int, val chunkZ: Int, val snapshot: ChunkSnapshot)
    private class Hit(val world: World, val x: Int, val y: Int, val z: Int)

    /** Entfernte Blöcke mit ihrem Originalzustand, in Entfern-Reihenfolge. */
    private val removed = LinkedHashMap<Block, BlockData>()

    /** Cache: hat dieses Material eine Block-Entity (Truhe, Ofen, Schild …)? */
    private val blockEntityCache = HashMap<Material, Boolean>()

    private var task: BukkitTask? = null

    /** true, solange eine Runde läuft (Scan, Entfernen, Warten oder Wiederherstellen). */
    var busy = false
        private set

    fun vanish(letter: Char): Boolean {
        if (busy) return false
        val settings = plugin.settings
        val materials = plugin.picker.materialsFor(letter)
        if (materials.isEmpty()) return false

        // Main-Thread: betroffene Chunks einsammeln (doppelte nur einmal) und Snapshots ziehen.
        val scans = Bukkit.getOnlinePlayers()
            .asSequence()
            .filter { it.gameMode != GameMode.SPECTATOR }
            .filter { settings.worlds.isEmpty() || it.world.name in settings.worlds }
            .map { it.location.chunk }
            .distinct()
            .map { ChunkScan(it.world, it.x, it.z, it.getChunkSnapshot(false, false, false)) }
            .toList()
        if (scans.isEmpty()) return false

        busy = true
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val hits = ArrayList<Hit>()
            for (scan in scans) {
                val minY = scan.world.minHeight
                val maxY = scan.world.maxHeight
                val baseX = scan.chunkX shl 4
                val baseZ = scan.chunkZ shl 4
                for (x in 0 until 16) for (z in 0 until 16) for (y in minY until maxY) {
                    if (scan.snapshot.getBlockType(x, y, z) in materials) {
                        hits += Hit(scan.world, baseX + x, y, baseZ + z)
                    }
                }
            }
            if (!plugin.isEnabled) return@Runnable
            Bukkit.getScheduler().runTask(plugin, Runnable { startRemoving(hits, materials) })
        })
        return true
    }

    private fun startRemoving(hits: List<Hit>, materials: Set<Material>) {
        val settings = plugin.settings
        val iterator = hits.iterator()
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            var done = 0
            while (done < settings.blocksPerTick && iterator.hasNext()) {
                val hit = iterator.next()
                val block = hit.world.getBlockAt(hit.x, hit.y, hit.z)
                val type = block.type
                // Seit dem Snapshot könnte sich der Block geändert haben.
                if (type !in materials) continue
                if (settings.skipBlockEntities && hasBlockEntity(block)) continue
                removed[block] = block.blockData
                block.setType(Material.AIR, false)
                done++
            }
            if (!iterator.hasNext()) {
                task?.cancel()
                plugin.logger.info("${removed.size} Blöcke entfernt.")
                task = Bukkit.getScheduler().runTaskLater(
                    plugin, Runnable { startRestoring() }, settings.durationSeconds * 20L,
                )
            }
        }, 0L, 1L)
    }

    private fun startRestoring() {
        val settings = plugin.settings
        var skipped = 0
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            // Jeden Tick neu, weil sich Spieler zwischen den Ticks bewegen.
            val occupied = blocksOccupiedByPlayers()
            var done = 0
            val iterator = removed.entries.iterator()
            while (done < settings.blocksPerTick && iterator.hasNext()) {
                val (block, data) = iterator.next()
                iterator.remove()
                if (!restore(block, data, occupied)) skipped++
                done++
            }
            if (removed.isEmpty()) {
                task?.cancel()
                task = null
                if (skipped > 0) plugin.logger.info("$skipped Blöcke nicht zurückgesetzt (Spieler stand drin).")
                busy = false
                plugin.onRoundFinished()
            }
        }, 0L, 1L)
    }

    /** Stellt sofort alles wieder her (für onDisable / Reload). */
    fun restoreAllNow() {
        task?.cancel()
        task = null
        val occupied = blocksOccupiedByPlayers()
        removed.forEach { (block, data) -> restore(block, data, occupied) }
        removed.clear()
        busy = false
    }

    /**
     * Setzt den Block zurück, außer dort steht inzwischen etwas anderes (von Spielern
     * platziert) oder ein Spieler würde darin stecken – dann bleibt der Block weg.
     */
    private fun restore(block: Block, data: BlockData, occupied: Set<Block>): Boolean {
        if (!block.type.isAir || block in occupied) return false
        block.setBlockData(data, false)
        return true
    }

    private fun hasBlockEntity(block: Block): Boolean =
        blockEntityCache.getOrPut(block.type) { block.getState(false) is TileState }

    /** Alle Block-Positionen, die von der Hitbox eines Spielers berührt werden. */
    private fun blocksOccupiedByPlayers(): Set<Block> {
        val result = HashSet<Block>()
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.gameMode == GameMode.SPECTATOR) continue
            val box = player.boundingBox
            val world = player.world
            for (x in floor(box.minX).toInt()..floor(box.maxX - EPS).toInt())
                for (y in floor(box.minY).toInt()..floor(box.maxY - EPS).toInt())
                    for (z in floor(box.minZ).toInt()..floor(box.maxZ - EPS).toInt())
                        result += world.getBlockAt(x, y, z)
        }
        return result
    }

    private companion object {
        const val EPS = 1.0e-7
    }
}
