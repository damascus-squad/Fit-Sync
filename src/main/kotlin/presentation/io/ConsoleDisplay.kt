package org.damascus.presentation.io

interface ConsoleDisplay {
    fun display(input: Any? = "")
    fun displayLn(input: Any? = "\n")
}