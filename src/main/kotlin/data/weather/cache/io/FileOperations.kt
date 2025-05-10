package org.damascus.data.weather.cache.io

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.opencsv.exceptions.CsvException
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class FileOperations(
    private val filePath: String= "csvCache"
) {
    private fun file(): File = File(filePath)

    // new 2 helper fun
    private fun ensureParentDirectoryExists() {
        file().parentFile
            ?.takeIf { !it.exists() }
            ?.mkdirs()
    }

    private fun withCsvWriter(
        append: Boolean,
        block: (CSVWriter) -> Unit
    ): Boolean = try {
        ensureParentDirectoryExists()
        FileWriter(filePath, append).use { fileWriter ->
            CSVWriter(fileWriter).use { csv ->
                // comment will deleted next update
                // why ? -> inject whatever CSV-writing code you need
                block(csv)
            }
        }
        true
    } catch (e: IOException) {
        false
    }


    fun fileExists(): Boolean = file().exists()

    fun writeHeader(header: Array<String>): Boolean =
        withCsvWriter(append = false) { csv ->
            csv.writeNext(header, false)
        }

    fun appendRow(row: Array<String>): Boolean =
        withCsvWriter(append = true) { csv ->
            csv.writeNext(row, false)
        }

    fun clearContent(header: Array<String>? = null): Boolean {
        return if (header == null) {
            withCsvWriter(append = false) { }
        } else {
            withCsvWriter(append = false) { csv ->
                csv.writeNext(header, false)
            }
        }
    }

    fun readAllRows(skipHeader: Boolean): List<Array<String>> {
        val target = file()
        if (!target.exists() || !target.canRead()) return emptyList()

        return try {
            FileReader(target).use { fileReader ->
                CSVReader(fileReader).use { csv ->
                    var rows = csv.readAll()
                        .filter { it.size >= 2 }
                    if (skipHeader) rows = rows.drop(1)
                    rows
                }
            }
        } catch (e: Exception) {
            if (e is IOException || e is CsvException) {
                emptyList()
            } else {
                throw e
            }
        }

    }

}