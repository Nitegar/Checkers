package de.htwg.view

object AnsiColor {
  val Reset = "\u001b[0m"
  val Red = "\u001b[91m"
  val BrightWhite = "\u001b[97m"
  val BrightBlack = "\u001b[90m"
  val Yellow = "\u001b[93m"
}

/**
 * Represents reusable ASCII art banners and effects used in the Checkers game.
 */
enum AsciiEffect(val art: String, val color: String = AnsiColor.Yellow) {

  // ───────────── TURN ANNOUNCEMENTS ─────────────
  case RedTurn extends AsciiEffect(
    s"""
╦═╗╔═╗╔╦╗  ╔╦╗╦ ╦╦═╗╔╗╔
╠╦╝║╣  ║║   ║ ║ ║╠╦╝║║║
╩╚═╚═╝═╩╝   ╩ ╚═╝╩╚═╝╚╝

      ○ RED'S TURN! ○
""",
    AnsiColor.BrightWhite
  )

  case BlackTurn extends AsciiEffect(
    s"""
╔╗ ╦  ╔═╗╔═╗╦╔═  ╔╦╗╦ ╦╦═╗╔╗╔
╠╩╗║  ╠═╣║  ╠╩╗   ║ ║ ║╠╦╝║║║
╚═╝╩═╝╩ ╩╚═╝╩ ╩   ╩ ╚═╝╩╚═╝╚╝

      ● BLACK'S TURN! ●
""",
    AnsiColor.BrightBlack
  )

  // ───────────── KILL EFFECTS ─────────────
  case SingleKill extends AsciiEffect(
    s"""
██╗  ██╗██╗██╗     ██╗     ██╗
██║ ██╔╝██║██║     ██║     ██║
█████╔╝ ██║██║     ██║     ██║
██╔═██╗ ██║██║     ██║     ╚═╝
██║  ██╗██║███████╗███████╗██╗
╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚═╝
        ⚡ K I L L ! ⚡
"""
  )

  case DoubleKill extends AsciiEffect(
    s"""
██████╗  ██████╗ ██╗   ██╗██████╗ ██╗     ███████╗
██╔══██╗██╔═══██╗██║   ██║██╔══██╗██║     ██╔════╝
██║  ██║██║   ██║██║   ██║██████╔╝██║     █████╗
██║  ██║██║   ██║██║   ██║██╔══██╗██║     ██╔══╝
██████╔╝╚██████╔╝╚██████╔╝██████╔╝███████╗███████╗
╚═════╝  ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚══════╝

    ⚡⚡ D O U B L E  K I L L ! ⚡⚡
"""
  )

  case TripleKill extends AsciiEffect(
    s"""
████████╗██████╗ ██╗██████╗ ██╗     ███████╗
╚══██╔══╝██╔══██╗██║██╔══██╗██║     ██╔════╝
   ██║   ██████╔╝██║██████╔╝██║     █████╗
   ██║   ██╔══██╗██║██╔═══╝ ██║     ██╔══╝
   ██║   ██║  ██║██║██║     ███████╗███████╗
   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝     ╚══════╝╚══════╝

  ⚡⚡⚡ T R I P L E  K I L L ! ⚡⚡⚡
"""
  )

  case UltraKill extends AsciiEffect(
    s"""
██╗   ██╗██╗  ████████╗██████╗  █████╗
██║   ██║██║  ╚══██╔══╝██╔══██╗██╔══██╗
██║   ██║██║     ██║   ██████╔╝███████║
██║   ██║██║     ██║   ██╔══██╗██╔══██║
╚██████╔╝███████╗██║   ██║  ██║██║  ██║
 ╚═════╝ ╚══════╝╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝

 ⚡⚡⚡⚡ U L T R A  K I L L ! ⚡⚡⚡⚡
"""
  )

  // ───────────── WIN SCREENS ─────────────
  case RedWins extends AsciiEffect(
    s"""
╦ ╦╦╔╗╔╔╗╔╔═╗╦═╗██╗
║║║║║║║║║║║╣ ╠╦╝██║
╚╩╝╩╝╚╝╝╚╝╚═╝╩╚═╚═╝

      ○ RED WINS! ○
""",
    AnsiColor.BrightWhite
  )

  case BlackWins extends AsciiEffect(
    s"""
╦ ╦╦╔╗╔╔╗╔╔═╗╦═╗██╗
║║║║║║║║║║║╣ ╠╦╝██║
╚╩╝╩╝╚╝╝╚╝╚═╝╩╚═╚═╝

      ● BLACK WINS! ●
""",
    AnsiColor.BrightBlack
  )
}
