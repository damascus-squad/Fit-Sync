package org.damascus.data.weather.cache.io

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission

class OpenCsvFileOperationsTest {

    private lateinit var fileOperations: OpenCsvFileOperations
    private lateinit var testBaseDir: Path

    @TempDir
    lateinit var rootTempDir: Path

    @BeforeEach
    fun setUp() {
        fileOperations = OpenCsvFileOperations()
        testBaseDir = Files.createDirectory(rootTempDir.resolve("csvOpsTestWorkingDir"))
    }

    private fun createFile(fileName: String, inDir: Path = testBaseDir): File {
        val file = inDir.resolve(fileName).toFile()
        file.parentFile?.mkdirs()
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    private fun createDirectory(dirName: String, inDir: Path = testBaseDir): File {
        val dir = inDir.resolve(dirName).toFile()
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()
        return dir
    }

    private fun createNonExistentFileReference(fileName: String, inDir: Path = testBaseDir): File {
        val file = inDir.resolve(fileName).toFile()
        if (file.exists()) {
            file.delete()
        }
        return file
    }

    private fun writeCsvLinesToFile(file: File, lines: List<String>) {
        file.bufferedWriter().use { writer ->
            lines.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }
    }

    @Test
    fun `fileExists - when file exists - returns true`() {
        val file = createFile("existing.csv")
        assertThat(fileOperations.fileExists(file.absolutePath)).isTrue()
    }

    @Test
    fun `fileExists - when file does not exist - returns false`() {
        val file = createNonExistentFileReference("non_existing.csv")
        assertThat(fileOperations.fileExists(file.absolutePath)).isFalse()
    }

    @Test
    fun `readAllRows - when file does not exist - returns emptyList`() {
        val file = createNonExistentFileReference("non_existing_read.csv")
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when path is a directory - returns emptyList (IOException caught)`() {
        val dir = createDirectory("a_directory_for_read")
        val result = fileOperations.readAllRows(dir.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file exists but is not readable (POSIX) - returns emptyList`() {
        val file = createFile("not_readable_posix.csv")
        writeCsvLinesToFile(file, listOf("data"))
        Files.getPosixFilePermissions(file.toPath())
        var madeUnreadable = false
        Files.setPosixFilePermissions(file.toPath(), setOf(PosixFilePermission.OWNER_WRITE))
        madeUnreadable = !file.canRead()

        if (madeUnreadable) {
            val result = fileOperations.readAllRows(file.absolutePath, false)
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `readAllRows - when file is empty - returns emptyList`() {
        val file = createFile("empty_read.csv")
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file has data - skipHeader false - returns all rows including header`() {
        val file = createFile("data_read_no_skip.csv")
        val expectedRows = listOf(
            arrayOf("header1", "header2"),
            arrayOf("value1", "value2"),
            arrayOf("value3", "value4")
        )
        writeCsvLinesToFile(file, expectedRows.map { it.joinToString(separator = ",") })
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `readAllRows - when file has data - skipHeader true - returns all rows except header`() {
        val file = createFile("data_read_skip.csv")
        writeCsvLinesToFile(file, listOf("header1,header2", "value1,value2", "value3,value4"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        val expectedRows = listOf(
            arrayOf("value1", "value2"),
            arrayOf("value3", "value4")
        )
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `readAllRows - skipHeader true, file starts with line of only commas - skips first line`() {
        val file = createFile("commas_header_skip.csv")
        writeCsvLinesToFile(file, listOf(",,,", "value1,value2", "value3,value4"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        val expectedRows = listOf(
            arrayOf("value1", "value2"),
            arrayOf("value3", "value4")
        )
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `readAllRows - skipHeader true, file is empty after header - returns emptyList`() {
        val file = createFile("header_then_empty_skip.csv")
        writeCsvLinesToFile(file, listOf("header1,header2"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file has only header - skipHeader true - returns emptyList`() {
        val file = createFile("only_header_skip.csv")
        writeCsvLinesToFile(file, listOf("header1,header2"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file has only header - skipHeader false - returns header row`() {
        val file = createFile("only_header_no_skip.csv")
        writeCsvLinesToFile(file, listOf("header1,header2"))
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result[0]).isEqualTo(arrayOf("header1", "header2"))
    }

    @Test
    fun `readAllRows - file with valid line then malformed line - skipHeader false - returns emptyList (CsvException)`() {
        val file = createFile("valid_then_malformed_no_skip.csv")
        writeCsvLinesToFile(file, listOf("ok1,ok2", "err1,\"unterminated_quote"))
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - file with valid header then malformed data line - skipHeader true - returns emptyList (CsvException)`() {
        val file = createFile("valid_header_then_malformed_data_skip.csv")
        writeCsvLinesToFile(file, listOf("headerA,headerB", "err1,\"unterminated_quote_after_header"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - malformed header line - skipHeader true - returns emptyList (CsvException)`() {
        val file = createFile("malformed_header_skip.csv")
        writeCsvLinesToFile(file, listOf("headerA,\"unterminated_header_quote", "data1,data2"))
        val result = fileOperations.readAllRows(file.absolutePath, true)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - malformed first line - skipHeader false - returns emptyList (CsvException)`() {
        val file = createFile("malformed_first_line_no_skip.csv")
        writeCsvLinesToFile(file, listOf("err1,\"unterminated_quote_at_start"))
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `appendRow - to new file - creates file and appends row`() {
        val file = createNonExistentFileReference("append_new.csv")
        val rowToAppend = arrayOf("new1", "new2")
        fileOperations.appendRow(file.absolutePath, rowToAppend)
        assertThat(file.exists()).isTrue()
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result[0]).isEqualTo(rowToAppend)
    }

    @Test
    fun `appendRow - to existing file - appends row to end`() {
        val file = createFile("append_existing.csv")
        writeCsvLinesToFile(file, listOf("old1,old2"))
        val rowToAppend = arrayOf("new1", "new2")
        fileOperations.appendRow(file.absolutePath, rowToAppend)
        val result = fileOperations.readAllRows(file.absolutePath, false)

        val expectedRows = listOf(
            arrayOf("old1", "old2"),
            rowToAppend
        )
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `appendRow - when path is a directory - does nothing (IOException caught)`() {
        val dir = createDirectory("a_directory_for_append")
        val initialFileCount = dir.listFiles()?.size ?: 0
        fileOperations.appendRow(dir.absolutePath, arrayOf("a", "b"))
        assertThat(dir.listFiles()?.size ?: 0).isEqualTo(initialFileCount)
    }

    @Test
    fun `writeHeader - to new file - creates file and writes header`() {
        val file = createNonExistentFileReference("header_new.csv")
        val header = arrayOf("h1", "h2")
        fileOperations.writeHeader(file.absolutePath, header)

        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result[0]).isEqualTo(header)
    }

    @Test
    fun `writeHeader - to existing file - overwrites file with header`() {
        val file = createFile("header_existing.csv")
        writeCsvLinesToFile(file, listOf("old1,old2"))
        val newHeader = arrayOf("nh1", "nh2")
        fileOperations.writeHeader(file.absolutePath, newHeader)
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result[0]).isEqualTo(newHeader)
    }

    @Test
    fun `writeHeader - when path is a directory - does nothing (IOException caught)`() {
        val dir = createDirectory("a_directory_for_write_header")
        val initialFileCount = dir.listFiles()?.size ?: 0
        fileOperations.writeHeader(dir.absolutePath, arrayOf("h", "g"))
        assertThat(dir.listFiles()?.size ?: 0).isEqualTo(initialFileCount)
    }

    @Test
    fun `clearFileContent - with header - clears file and writes header`() {
        val file = createFile("clear_with_header.csv")
        writeCsvLinesToFile(file, listOf("old_h1,old_h2", "old_v1,old_v2"))
        val newHeader = arrayOf("new_h1", "new_h2")
        val success = fileOperations.clearFileContent(file.absolutePath, newHeader)
        assertThat(success).isTrue()
        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(newHeader)
    }

    @Test
    fun `clearFileContent - without header - clears file and leaves it empty`() {
        val file = createFile("clear_no_header.csv")
        writeCsvLinesToFile(file, listOf("old_h1,old_h2", "old_v1,old_v2"))
        fileOperations.clearFileContent(file.absolutePath, null)
        assertThat(file.length()).isEqualTo(0L)
    }

    @Test
    fun `clearFileContent - when file does not exist - with header - creates file with header`() {
        val file = createNonExistentFileReference("clear_new_with_header.csv")
        val header = arrayOf("h1", "h2")
        fileOperations.clearFileContent(file.absolutePath, header)

        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result[0]).isEqualTo(header)
    }

    @Test
    fun `clearFileContent - when file does not exist - without header - creates empty file`() {
        val file = createNonExistentFileReference("clear_new_no_header.csv")
        fileOperations.clearFileContent(file.absolutePath, null)

        val result = fileOperations.readAllRows(file.absolutePath, false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `clearFileContent - when path is a directory - returns false (IOException caught)`() {
        val dir = createDirectory("a_directory_for_clear")
        val initialFileCount = dir.listFiles()?.size ?: 0
        val success = fileOperations.clearFileContent(dir.absolutePath, arrayOf("h", "g"))
        assertThat(success).isFalse()
        assertThat(dir.listFiles()?.size ?: 0).isEqualTo(initialFileCount)
    }

}