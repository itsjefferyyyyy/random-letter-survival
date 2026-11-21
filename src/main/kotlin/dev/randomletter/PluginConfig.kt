package dev.randomletter

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import java.util.logging.Logger

data class PluginConfig(
    val intervalSeconds: Int,
    val durationSeconds: Int,
    val warningSeconds: Int,
    val blocksPerTick: Int,
    val skipBlockEntities: Boolean,
    val worlds: Set<String>,
    val excludedMaterials: Set<Material>,
) {
    companion object {
        fun from(config: FileConfiguration, logger: Logger): PluginConfig {
            val excluded = config.getStringList("excluded-materials").mapNotNull { name ->
                Material.matchMaterial(name) ?: run {
                    logger.warning("Unbekanntes Material in excluded-materials: $name")
                    null
                }
            }.toSet()

            return PluginConfig(
                intervalSeconds = config.getInt("interval-seconds", 180).coerceAtLeast(5),
                durationSeconds = config.getInt("duration-seconds", 20).coerceAtLeast(1),
                warningSeconds = config.getInt("warning-seconds", 10).coerceAtLeast(0),
                blocksPerTick = config.getInt("blocks-per-tick", 5000).coerceAtLeast(100),
                skipBlockEntities = config.getBoolean("skip-block-entities", true),
                worlds = config.getStringList("worlds").toSet(),
                excludedMaterials = excluded,
            )
        }
    }
}
