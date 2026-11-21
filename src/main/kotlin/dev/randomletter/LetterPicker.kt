package dev.randomletter

import org.bukkit.Material
import java.util.EnumSet
import kotlin.random.Random

/**
 * Gruppiert Block-Materialien nach dem Anfangsbuchstaben ihrer englischen ID
 * und zieht einen Buchstaben, gewichtet nach der Anzahl der Materialien.
 */
class LetterPicker(materials: Collection<Material>) {

    private val byLetter: Map<Char, Set<Material>> = materials
        .groupBy { it.name.first().uppercaseChar() }
        .filterKeys { it in 'A'..'Z' }
        .mapValues { (_, list) -> EnumSet.copyOf(list) }

    private val totalWeight = byLetter.values.sumOf { it.size }

    /** Alle Buchstaben, für die es mindestens ein Material gibt. */
    val letters: Set<Char> get() = byLetter.keys

    fun materialsFor(letter: Char): Set<Material> =
        byLetter[letter.uppercaseChar()] ?: emptySet()

    fun pick(random: Random = Random.Default): Char {
        check(totalWeight > 0) { "Keine Materialien verfügbar" }
        var roll = random.nextInt(totalWeight)
        for ((letter, set) in byLetter) {
            roll -= set.size
            if (roll < 0) return letter
        }
        error("unreachable")
    }

    companion object {
        @Suppress("DEPRECATION")
        fun fromRegistry(excluded: Set<Material>): LetterPicker = LetterPicker(
            Material.entries.filter { it.isBlock && !it.isAir && !it.isLegacy && it !in excluded }
        )
    }
}
