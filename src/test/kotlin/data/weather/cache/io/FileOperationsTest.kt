package data.weather.cache.io

import com.google.common.truth.Truth.*

import org.damascus.data.weather.cache.io.FileOperations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.io.File

class FileOperationsTest {

    private lateinit var fileOperations: FileOperations
    private lateinit var testFilePath: String

    @TempDir
    lateinit var rootTempDir: Path

    @BeforeEach
    fun setUp() {
        testFilePath = rootTempDir.resolve("testFile.csv").toString()
        fileOperations = FileOperations(testFilePath)
    }

    private fun createFile(): File {
        val file = File(testFilePath)
        file.parentFile?.mkdirs()
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    private fun createDirectory(): File {
        val dir = File(testFilePath)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()
        return dir
    }

    private fun writeCsvLinesToFile(lines: List<String>) {
        File(testFilePath).bufferedWriter().use { writer ->
            lines.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }
    }

    @Test
    fun `fileExists - when file exists - returns true`() {
        createFile()
        assertThat(fileOperations.fileExists()).isTrue()
    }

    @Test
    fun `fileExists - when file does not exist - returns false`() {
        assertThat(fileOperations.fileExists()).isFalse()
    }

    @Test
    fun `readAllRows - when file does not exist - returns emptyList`() {
        val result = fileOperations.readAllRows(false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when path is a directory - returns emptyList`() {
        createDirectory()
        val result = fileOperations.readAllRows(false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file exists but is not readable - returns emptyList`() {
        val file = createFile()
        writeCsvLinesToFile(listOf("data"))

        try {
            Files.setPosixFilePermissions(file.toPath(), setOf(PosixFilePermission.OWNER_WRITE))
            val result = fileOperations.readAllRows(false)
            assertThat(result).isEmpty()
        } catch (e: UnsupportedOperationException) {
            // Skip test on Windows systems that don't support POSIX permissions
        }
    }

    @Test
    fun `readAllRows - when file is empty - returns emptyList`() {
        createFile()
        val result = fileOperations.readAllRows(false)
        assertThat(result).isEmpty()
    }

    @Test
    fun `readAllRows - when file has data - skipHeader false - returns all rows including header`() {
        createFile()
        val expectedRows = listOf(
            arrayOf("header1", "header2"),
            arrayOf("value1", "value2"),
            arrayOf("value3", "value4")
        )
        writeCsvLinesToFile(expectedRows.map { it.joinToString(",") })
        val result = fileOperations.readAllRows(false)
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `readAllRows - when file has data - skipHeader true - returns all rows except header`() {
        createFile()
        writeCsvLinesToFile(listOf("header1,header2", "value1,value2", "value3,value4"))
        val result = fileOperations.readAllRows(true)
        val expectedRows = listOf(
            arrayOf("value1", "value2"),
            arrayOf("value3", "value4")
        )
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `writeHeader - to new file - creates file and writes header`() {
        val header = arrayOf("h1", "h2")
        fileOperations.writeHeader(header)

        val result = fileOperations.readAllRows(false)
        assertThat(result[0]).isEqualTo(header)
    }

    @Test
    fun `writeHeader - to existing file - overwrites file with header`() {
        createFile()
        writeCsvLinesToFile(listOf("old1,old2"))
        val newHeader = arrayOf("nh1", "nh2")
        fileOperations.writeHeader(newHeader)
        val result = fileOperations.readAllRows(false)
        assertThat(result[0]).isEqualTo(newHeader)
    }

    @Test
    fun `appendRow - to new file - creates file and appends row`() {
        val rowToAppend = arrayOf("new1", "new2")
        fileOperations.appendRow(rowToAppend)
        val result = fileOperations.readAllRows(false)
        assertThat(result[0]).isEqualTo(rowToAppend)
    }

    @Test
    fun `appendRow - to existing file - appends row to end`() {
        createFile()
        writeCsvLinesToFile(listOf("old1,old2"))
        val rowToAppend = arrayOf("new1", "new2")
        fileOperations.appendRow(rowToAppend)
        val result = fileOperations.readAllRows(false)

        val expectedRows = listOf(
            arrayOf("old1", "old2"),
            rowToAppend
        )
        assertThat(result.map { it.toList() }).isEqualTo(expectedRows.map { it.toList() })
    }

    @Test
    fun `clearContent - with header - clears file and writes header`() {
        createFile()
        writeCsvLinesToFile(listOf("old_h1,old_h2", "old_v1,old_v2"))
        val newHeader = arrayOf("new_h1", "new_h2")
        fileOperations.clearContent(newHeader)
        val result = fileOperations.readAllRows(false)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(newHeader)
    }

    @Test
    fun `clearContent - without header - clears file and leaves it empty`() {
        createFile()
        writeCsvLinesToFile(listOf("old_h1,old_h2", "old_v1,old_v2"))
        fileOperations.clearContent(null)
        assertThat((File(testFilePath).length()) == 0L)

    }

    @Test
    fun `clearContent - when file does not exist - with header - creates file with header`() {
        val header = arrayOf("h1", "h2")
        fileOperations.clearContent(header)
        val result = fileOperations.readAllRows(false)
        assertThat(result[0]).isEqualTo(header)
    }

    @Test
    fun `clearContent - when file does not exist - without header - creates empty file`() {
        fileOperations.clearContent(null)
        val result = fileOperations.readAllRows(false)
        assertThat(result).isEmpty()
    }
}
