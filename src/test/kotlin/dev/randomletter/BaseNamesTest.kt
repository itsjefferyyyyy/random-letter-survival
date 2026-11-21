package dev.randomletter

import kotlin.test.Test
import kotlin.test.assertEquals

class BaseNamesTest {

    @Test
    fun `Varianten zaehlen zum Grundmaterial`() {
        val expected = mapOf(
            "GRASS_BLOCK" to "DIRT",
            "COARSE_DIRT" to "DIRT",
            "PODZOL" to "DIRT",
            "POLISHED_ANDESITE" to "ANDESITE",
            "MOSSY_COBBLESTONE" to "COBBLESTONE",
            "COBBLESTONE_STAIRS" to "COBBLESTONE_STAIRS",
            "SPRUCE_LOG" to "SPRUCE_LOG",
            "STRIPPED_SPRUCE_LOG" to "SPRUCE_LOG",
            "OAK_PLANKS" to "OAK_PLANKS",
            "DEEPSLATE_IRON_ORE" to "IRON_ORE",
            "DEEPSLATE" to "DEEPSLATE",
            "DEEPSLATE_BRICKS" to "DEEPSLATE_BRICKS",
            "CRACKED_DEEPSLATE_TILES" to "DEEPSLATE_TILES",
            "WAXED_EXPOSED_CUT_COPPER" to "COPPER",
            "WALL_TORCH" to "TORCH",
            "STONE" to "STONE",
            "SMOOTH_STONE" to "STONE",
        )
        for ((id, base) in expected) assertEquals(base, BaseNames.baseName(id), id)
    }

    @Test
    fun `Buchstaben`() {
        assertEquals('D', BaseNames.letterOf("GRASS_BLOCK"))
        assertEquals('C', BaseNames.letterOf("COBBLESTONE_STAIRS"))
        assertEquals('I', BaseNames.letterOf("DEEPSLATE_IRON_ORE"))
    }

    @Test
    fun `eigene Aliase aus der Config`() {
        val aliases = BaseNames.DEFAULT_ALIASES + ("MUD" to "DIRT")
        assertEquals('D', BaseNames.letterOf("MUD", aliases))
        assertEquals('M', BaseNames.letterOf("MUD"))
    }
}
