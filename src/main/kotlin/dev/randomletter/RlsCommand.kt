package dev.randomletter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class RlsCommand(private val plugin: RandomLetterPlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.firstOrNull()?.lowercase()) {
            "start" -> sender.reply(if (plugin.startTimer()) "Timer gestartet." else "Timer läuft bereits.")
            "stop" -> sender.reply(if (plugin.stopTimer()) "Timer gestoppt." else "Timer läuft nicht.")
            "trigger" -> {
                val arg = args.getOrNull(1)
                val forced = arg?.singleOrNull()?.uppercaseChar()
                if (arg != null && (forced == null || forced !in plugin.picker.letters)) {
                    sender.reply("Ungültiger Buchstabe: $arg", NamedTextColor.RED)
                    return true
                }
                if (!plugin.runRound(forced)) sender.reply("Runde konnte nicht starten (läuft schon eine oder keine Spieler/Chunks).", NamedTextColor.RED)
            }
            "reload" -> {
                plugin.loadSettings()
                if (plugin.stopTimer()) plugin.startTimer()
                sender.reply("Config neu geladen.")
            }
            else -> return false
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> =
        when (args.size) {
            1 -> listOf("start", "stop", "trigger", "reload").filter { it.startsWith(args[0].lowercase()) }
            2 if args[0].equals("trigger", ignoreCase = true) ->
                plugin.picker.letters.sorted().map(Char::toString).filter { it.startsWith(args[1].uppercase()) }
            else -> emptyList()
        }

    private fun CommandSender.reply(text: String, color: NamedTextColor = NamedTextColor.GREEN) =
        sendMessage(Component.text(text, color))
}
