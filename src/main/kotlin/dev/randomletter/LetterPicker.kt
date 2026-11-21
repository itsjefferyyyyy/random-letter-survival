package dev.randomletter

import org.bukkit.Material
import java.util.EnumSet
import kotlin.random.Random

/**
 * Gruppiert Block-Materialien nach dem Anfangsbuchstaben ihres Grundnamens
 * (siehe [BaseNames], z.B. GRASS_BLOCK → DIRT → D) und zieht einen Buchstaben, gewichtet nach den Materialien, die wirklich vorkommen.
 */
class LetterPicker(
    materials: Collection<Material>,
    aliases: Map<String, String> = BaseNames.DEFAULT_ALIASES,
) {

    private val byLetter: Map<Char, Set<Material>> = materials
        .groupBy { BaseNames.letterOf(it.name, aliases) }
        .filterKeys { it in 'A'..'Z' }
        .mapValues { (_, list) -> EnumSet.copyOf(list) }

    /** Alle Buchstaben, für die es mindestens ein Material gibt. */
    val letters: Set<Char> get() = byLetter.keys

    fun materialsFor(letter: Char): Set<Material> =
        byLetter[letter.uppercaseChar()] ?: emptySet()

    /**
     * Zieht einen Buchstaben aus den Materialien, die tatsächlich vorkommen.
     * Gewicht = Anzahl verschiedener vorhandener Materialien mit diesem Buchstaben
     * (z.B. Stone + Sand + Spruce Log vorhanden → S hat Gewicht 3).
     * Gibt null zurück, wenn kein erlaubtes Material vorkommt.
     */
    fun pickFrom(present: Set<Material>, random: Random = Random.Default): Char? {
        val weights = byLetter.mapValues { (_, set) -> set.count { it in present } }.filterValues { it > 0 }
        val total = weights.values.sum()
        if (total == 0) return null
        var roll = random.nextInt(total)
        for ((letter, weight) in weights) {
            roll -= weight
            if (roll < 0) return letter
        }
        error("unreachable")
    }

    companion object {
        @Suppress("DEPRECATION")
        fun fromRegistry(excluded: Set<Material>, aliases: Map<String, String>): LetterPicker = LetterPicker(
            Material.entries.filter { it.isBlock && !it.isAir && !it.isLegacy && it !in excluded },
            aliases,
        )
    }
}
