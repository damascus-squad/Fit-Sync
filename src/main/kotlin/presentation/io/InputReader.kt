package presentation.io

interface InputReader {
    fun readString(prompt: String): String
    fun readInt(prompt: String, min: Int?, max: Int?):Int
}