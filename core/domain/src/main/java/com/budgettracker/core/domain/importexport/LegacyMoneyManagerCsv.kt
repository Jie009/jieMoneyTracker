package com.budgettracker.core.domain.importexport

object LegacyMoneyManagerCsvReader {
    fun read(text: String): LegacyImportPreview {
        val rows = parseCsv(text)
        if (rows.isEmpty()) return LegacyImportPreview(records = emptyList(), errors = emptyList())
        val headers = rows.first()
        val dataRows = rows.drop(1).map { row ->
            headers.mapIndexed { index, header ->
                header to row.getOrElse(index) { "" }
            }.toMap()
        }

        return LegacyMoneyManagerParser.parseRows(dataRows)
    }

    private fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < text.length) {
            val char = text[index]
            when {
                char == '"' && inQuotes && text.getOrNull(index + 1) == '"' -> {
                    cell.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    row += cell.toString()
                    cell.clear()
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && text.getOrNull(index + 1) == '\n') index++
                    row += cell.toString()
                    cell.clear()
                    if (row.any { it.isNotBlank() }) rows += row.toList()
                    row.clear()
                }
                else -> cell.append(char)
            }
            index++
        }

        row += cell.toString()
        if (row.any { it.isNotBlank() }) rows += row.toList()
        return rows
    }
}

object LegacyMoneyManagerCsvWriter {
    fun write(rows: List<List<String>>): String =
        rows.joinToString(separator = "\n") { row ->
            row.joinToString(separator = ",") { cell -> cell.escapeCsv() }
        }

    private fun String.escapeCsv(): String =
        if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }
}
