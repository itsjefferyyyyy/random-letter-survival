package dev.randomletter

/**
 * Bestimmt den "ursprünglichen" Namen eines Blocks, nach dem sein Buchstabe gewählt wird.
 * Varianten zählen zum Grundmaterial:
 *  - Bearbeitungen fallen weg:   POLISHED_ANDESITE  → ANDESITE
 *  - Erz-Varianten:               DEEPSLATE_IRON_ORE → IRON_ORE
 *  - Sonderfälle per Tabelle:     GRASS_BLOCK        → DIRT
 * Formen (Stairs, Slab, Wall …) stehen hinten und ändern den Anfangsbuchstaben nicht.
 */
object BaseNames {

    /** Standard-Sonderfälle; können in der config.yml unter `letter-aliases` ergänzt/überschrieben werden. */
    val DEFAULT_ALIASES: Map<String, String> = mapOf(
        "GRASS_BLOCK" to "DIRT",
        "PODZOL" to "DIRT",
        "MYCELIUM" to "DIRT",
        "ROOTED_DIRT" to "DIRT",
        "DIRT_PATH" to "DIRT",
        "FARMLAND" to "DIRT",
    )

    /** Vorsilben, die nur eine Variante des Grundblocks beschreiben. */
    private val MODIFIER_PREFIXES = listOf(
        "WAXED_", "EXPOSED_", "WEATHERED_", "OXIDIZED_",
        "POLISHED_", "CRACKED_", "MOSSY_", "CHISELED_", "SMOOTH_", "CUT_",
        "COARSE_", "INFESTED_", "STRIPPED_", "SUSPICIOUS_", "DEAD_",
        "CHIPPED_", "DAMAGED_", "WALL_",
    )

    fun baseName(id: String, aliases: Map<String, String> = DEFAULT_ALIASES): String {
        var name = id.uppercase()
        // Wiederholen, weil Vorsilben kombiniert sein können (WAXED_EXPOSED_CUT_COPPER → COPPER).
        while (true) {
            aliases[name]?.let { return it.uppercase() }
            val prefix = MODIFIER_PREFIXES.firstOrNull { name.startsWith(it) && name.length > it.length }
                ?: if (name.startsWith("DEEPSLATE_") && name.endsWith("_ORE")) "DEEPSLATE_" else null
                ?: return name
            name = name.removePrefix(prefix)
        }
    }

    fun letterOf(id: String, aliases: Map<String, String> = DEFAULT_ALIASES): Char =
        baseName(id, aliases).first()
}
