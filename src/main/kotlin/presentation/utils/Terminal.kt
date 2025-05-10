package presentation.utils

import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType


enum class TerminalColor(val code: String) {
    Red("\u001B[31m"),
    Green("\u001B[32m"),
    Yellow("\u001B[33m"),
    Blue("\u001B[34m"),
    Magenta("\u001B[35m"),
    Cyan("\u001B[36m"),
    Reset("\u001B[0m");

    fun wrap(text: String) = "$code$text${Reset.code}"
}

fun String.withStyle(color: TerminalColor) = color.wrap(this)

fun enableWindowsAnsi() {
    if (System.getProperty("os.name").contains("Windows"))
        System.setProperty("jansi.passthrough", "true")
}

fun clearConsole() {
    Runtime.getRuntime().exec(
        if (System.getProperty("os.name").contains("Windows")) "cls" else "clear"
    )
    println("\u001B[2J")
}

fun List<Cloth>.printGroupedByType() {
    val grouped = this.groupBy { it.type }

    grouped.forEach { (type, items) ->
        val color = when (type) {
            ClothType.VERY_HEAVY -> TerminalColor.Red
            ClothType.HEAVY -> TerminalColor.Magenta
            ClothType.MEDIUM -> TerminalColor.Blue
            ClothType.LIGHT -> TerminalColor.Cyan
            ClothType.VERY_LIGHT -> TerminalColor.Green
        }

        val title = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        println("\n${title.withStyle(color)}:")
        println("=".repeat(title.length + 1).withStyle(color))

        items.forEach { cloth ->
            println("• ${cloth.name}".withStyle(color))
        }
    }
}
