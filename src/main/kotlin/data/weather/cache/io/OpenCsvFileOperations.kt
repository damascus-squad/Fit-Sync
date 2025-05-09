package org.damascus.data.weather.cache.io

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.opencsv.exceptions.CsvException
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class OpenCsvFileOperations : CsvFileOperations {

    override fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    override fun readAllRows(filePath: String, skipHeader: Boolean): List<Array<String>> {
        val file = File(filePath)
        if (!file.exists() || !file.canRead()) return emptyList()

        try {
            FileReader(file).use { fileReader ->
                CSVReader(fileReader).use { csvReader ->
                    if (skipHeader && csvReader.peek()?.isNotEmpty() == true) {
                        csvReader.readNext()
                    }
                    return csvReader.readAll()
                }
            }
        } catch (e: IOException) {
            return emptyList()
        } catch (e: CsvException) {
            return emptyList()
        }
    }

    override fun appendRow(filePath: String, row: Array<String>) {
        try {
            FileWriter(filePath, true).use { fileWriter ->
                CSVWriter(fileWriter).use { csvWriter ->
                    csvWriter.writeNext(row, false)
                    csvWriter.flush()
                }
            }
        } catch (e: IOException) {

        }
    }

    override fun writeHeader(filePath: String, header: Array<String>) {
        try {
            FileWriter(filePath, false).use { fileWriter ->
                CSVWriter(fileWriter).use { csvWriter ->
                    csvWriter.writeNext(header, false)
                    csvWriter.flush()
                }
            }
        } catch (e: IOException) {

        }
    }

    override fun clearFileContent(filePath: String, header: Array<String>?): Boolean {
        try {
            FileWriter(filePath, false).use { fileWriter ->
                if (header != null) {
                    CSVWriter(fileWriter).use { csvWriter ->
                        csvWriter.writeNext(header, false)
                        csvWriter.flush()
                    }
                }
            }
            return true
        } catch (e: IOException) {
            return false
        }
    }
}