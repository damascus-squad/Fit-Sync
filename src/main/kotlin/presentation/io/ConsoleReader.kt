package presentation.io

import presentation.utils.TerminalColor
import presentation.utils.withStyle

class ConsoleReader : InputReader {

    override fun readString(prompt: String): String {
        print("$prompt ".withStyle(TerminalColor.Blue))
        while (true) {
            val input = readln().trim()

            if (!isValidName(input)) {
                println("❌ Invalid input! Please enter letters only.".withStyle(TerminalColor.Red))
                print("$prompt ".withStyle(TerminalColor.Blue))
            } else {
                return input
            }
        }
    }

    private fun isValidName(input: String): Boolean {
        return input.matches(Regex("^[a-zA-Z\\s'-]{2,}$"))
    }

    override fun readInt(prompt: String, min: Int?, max: Int?): Int {
        print("$prompt ".withStyle(TerminalColor.Blue))
        while (true) {
            val input = readlnOrNull()?.trim()?.toIntOrNull() ?: throw Exception("Wrong input: ")

            if ((min != null && input < min) || (max != null && input > max)) {
                println("❌ Invalid input. Try again.\n".withStyle(TerminalColor.Red))
            } else return input
        }
    }
}