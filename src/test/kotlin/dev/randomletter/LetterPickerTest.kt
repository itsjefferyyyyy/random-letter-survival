package dev.randomletter

import org.bukkit.Material
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
    fun `zieht nur Buchstaben, deren Bloecke vorkommen`() {
        val random = Random(42)
        val present = setOf(Material.STONE, Material.DIRT)
        val counts = (1..1_000).groupingBy { picker.pickFrom(present, random) }.eachCount()
        assertEquals(setOf<Char?>('S', 'D'), counts.keys)
    }

    @Test
    fun `gewichtet nach vorhandenen Materialien`() {
        val random = Random(42)
        // S: Stone, Sand, Spruce Log vorhanden (3) – D: Dirt (1) → ungefähr 75 % / 25 %
        val present = setOf(Material.STONE, Material.SAND, Material.SPRUCE_LOG, Material.DIRT)
        val counts = (1..10_000).groupingBy { picker.pickFrom(present, random) }.eachCount()
        val ratio = counts.getValue('S') / 10_000.0
        assertTrue(ratio in 0.72..0.78, "S-Anteil war $ratio")
    }

    @Test
    fun `null wenn nichts Passendes vorkommt`() {
        assertNull(picker.pickFrom(setOf(Material.AIR, Material.BEDROCK)))
        assertNull(picker.pickFrom(emptySet()))
    }
}
