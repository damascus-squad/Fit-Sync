package org.damascus.data.weather.cache.io

interface CsvFileOperations {
    fun fileExists(filePath: String): Boolean
    fun readAllRows(filePath: String, skipHeader: Boolean = true): List<Array<String>>
    fun appendRow(filePath: String, row: Array<String>)
    fun writeHeaderAndRow(filePath: String, header: Array<String>, row: Array<String>)
    fun writeHeader(filePath: String, header: Array<String>)
}