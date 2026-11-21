package dev.randomletter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.time.Duration

class RandomLetterPlugin : JavaPlugin() {

    lateinit var settings: PluginConfig
        private set
    lateinit var picker: LetterPicker
        private set
    private lateinit var vanishManager: VanishManager

    private var timerTask: BukkitTask? = null
    private var secondsLeft = 0

    val timerRunning: Boolean get() = timerTask != null

    override fun onEnable() {
        saveDefaultConfig()
        loadSettings()
        vanishManager = VanishManager(this)
        getCommand("rls")?.let {
            val command = RlsCommand(this)
            it.setExecutor(command)
            it.tabCompleter = command
        }
        startTimer()
    }

    override fun onDisable() {
        stopTimer()
        if (::vanishManager.isInitialized) vanishManager.restoreAllNow()
    }

    fun loadSettings() {
        reloadConfig()
        settings = PluginConfig.from(config, logger)
        picker = LetterPicker.fromRegistry(settings.excludedMaterials)
    }

    fun startTimer(): Boolean {
        if (timerTask != null) return false
        secondsLeft = settings.intervalSeconds
        timerTask = server.scheduler.runTaskTimer(this, Runnable { tick() }, 20L, 20L)
        return true
    }

    fun stopTimer(): Boolean {
        val task = timerTask ?: return false
        task.cancel()
        timerTask = null
        return true
    }

    private fun tick() {
        // Während eine Runde läuft, wird der Countdown angehalten.
        if (vanishManager.busy) return
        secondsLeft--
        if (settings.warningSeconds > 0 && secondsLeft == settings.warningSeconds) {
            Bukkit.broadcast(
                Component.text("In ${settings.warningSeconds} Sekunden verschwindet ein Buchstabe!", NamedTextColor.YELLOW)
            )
        }
        if (secondsLeft <= 0) {
            runRound()
            secondsLeft = settings.intervalSeconds
        }
    }

    /** Startet eine Runde. Gibt den Buchstaben zurück oder null, wenn nichts passiert ist. */
    fun runRound(forced: Char? = null): Char? {
        if (vanishManager.busy) return null
        val letter = forced?.uppercaseChar() ?: picker.pick()
        if (!vanishManager.vanish(letter)) return null

        val title = Title.title(
            Component.text(letter.toString(), NamedTextColor.RED),
            Component.text("Alle Blöcke mit $letter verschwinden für ${settings.durationSeconds}s!", NamedTextColor.GOLD),
            Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500)),
        )
        Bukkit.getServer().showTitle(title)
        Bukkit.broadcast(
            Component.text("Buchstabe ", NamedTextColor.GOLD)
                .append(Component.text(letter.toString(), NamedTextColor.RED))
                .append(Component.text(" – alle Blöcke damit verschwinden für ${settings.durationSeconds} Sekunden!", NamedTextColor.GOLD))
        )
        return letter
    }

    fun onRoundFinished() {
        Bukkit.broadcast(Component.text("Die Blöcke sind zurück.", NamedTextColor.GREEN))
    }
}
