package dev.randomletter

import org.bukkit.Material
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LetterPickerTest {

    private val picker = LetterPicker(
        listOf(Material.STONE, Material.SAND, Material.SPRUCE_LOG, Material.DIRT)
    )

    @Test
    fun `gruppiert nach Anfangsbuchstaben`() {
        assertEquals(setOf('S', 'D'), picker.letters)
        assertEquals(setOf(Material.STONE, Material.SAND, Material.SPRUCE_LOG), picker.materialsFor('s'))
        assertTrue(picker.materialsFor('Q').isEmpty())
    }

    @Test
    fun `zieht nur vorhandene Buchstaben, gewichtet`() {
        val random = Random(42)
        val counts = (1..10_000).groupingBy { picker.pick(random) }.eachCount()
        assertEquals(setOf('S', 'D'), counts.keys)
        // S hat 3 Materialien, D nur 1 → ungefähr 75 % / 25 %
        val ratio = counts.getValue('S') / 10_000.0
        assertTrue(ratio in 0.72..0.78, "S-Anteil war $ratio")
    }
}
