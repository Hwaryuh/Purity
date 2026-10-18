package kr.hwaryuh.purity.guard

import kr.hwaryuh.poly.config.ConfigurateConfigDocuments
import kr.hwaryuh.poly.platform.PolyPlugin
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.nio.file.Files
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFails
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GuardTest {
    @Test
    fun namesAndNamespaceAliasesAreBlocked() {
        val names = setOf("plugins", "ns:plugins", "version", "ns:version", "roll", "other:roll", "help")
        val blocked = GuardSnapshot.of(GuardOptions(blockedCommands = setOf(" Seed ", "NS:", "roll"))).blockedCommands
        for (label in listOf("seed", "other:seed", "roll", "ROLL", "other:roll", "ns:anything", "plugins", "Version")) {
            assertTrue(GuardPolicy.isBlocked(label, blocked, names::contains), label)
        }
        for (label in listOf("ns", "other:help", "help")) {
            assertFalse(GuardPolicy.isBlocked(label, blocked, names::contains), label)
        }
        assertFalse(GuardPolicy.isBlocked("plugins", emptySet(), names::contains))
    }

    @Test
    fun filterUsesOneLiveSnapshotForBlockingAndVisibility() {
        val player = UUID.randomUUID()
        val rules = object : GuardRules {
            override var snapshot = GuardSnapshot.of(GuardOptions(blockedCommands = setOf("seed")))
        }
        val filter = GuardFilter(rules)
        assertTrue(filter.blocks(player, " \tSeEd\t42") { false })
        assertEquals(listOf("seed"), filter.hidden(player, setOf("seed", "help")))
        rules.snapshot = GuardSnapshot.of(GuardOptions(setOf(player), setOf("seed")))
        assertFalse(filter.blocks(player, "seed") { false })
        assertTrue(filter.hidden(player, setOf("seed")).isEmpty())
        rules.snapshot = GuardSnapshot.of(GuardOptions())
        assertFalse(filter.blocks(player, "seed") { false })
    }

    @Test
    fun rejectionKeepsVanillaTwoLineShapeAndSuggestion() {
        val rules = object : GuardRules {
            override val snapshot = GuardSnapshot.of(GuardOptions())
        }
        val lines = GuardFilter(rules).rejection("seed 42")
        assertEquals(2, lines.size)
        assertTrue(lines.all { it.color() == NamedTextColor.RED })
        val suggestion = lines.last().clickEvent()!!.payload() as ClickEvent.Payload.Text
        assertEquals("/seed 42", suggestion.value())
    }

    @Test
    fun fileCreationAndReloadPreservePublishedSnapshotOnFailure() {
        val directory = Files.createTempDirectory("purity-guard-")
        val plugin = object : PolyPlugin {
            override val name = "Purity"
            override val dataDirectory = directory
        }
        val file = directory.resolve("guard.yml")
        try {
            val config = GuardConfig(plugin, ConfigurateConfigDocuments())
            config.snapshot = config.read()
            assertTrue(Files.exists(file))
            assertTrue(config.snapshot.blockedCommands.isEmpty())
            val id = UUID.randomUUID()
            val text = "# keep\nbypass-players: [$id]\nblocked-commands: [' Plugins ', 'BUKKIT:']\n"
            Files.writeString(file, text)
            config.snapshot = config.read()
            assertEquals(setOf(id), config.snapshot.bypassPlayers)
            assertEquals(setOf("plugins", "bukkit:"), config.snapshot.blockedCommands)
            assertEquals(text, Files.readString(file))
            val previous = config.snapshot
            for (invalid in listOf("bypass-players: [", "bypass-players: [bad]", "blocked-commands: seed", "blocked-commands: [7]", "seed", "[]", "true")) {
                Files.writeString(file, invalid)
                assertFails { config.snapshot = config.read() }
                assertSame(previous, config.snapshot)
            }
        } finally {
            Files.deleteIfExists(file)
            Files.deleteIfExists(directory)
        }
    }
}
