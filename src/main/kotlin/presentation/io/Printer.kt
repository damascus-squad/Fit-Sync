package org.damascus.presentation.io

interface Printer {
    fun display(input: Any? = "")
    fun displayLn(input: Any? = "\n")
}