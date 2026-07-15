package com.budgettracker.core.domain.importexport

import org.w3c.dom.Element
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object LegacyMoneyManagerXlsxReader {
    fun read(inputStream: InputStream): LegacyImportPreview {
        val files = unzip(inputStream)
        val sharedStrings = files["xl/sharedStrings.xml"]
            ?.let(::readSharedStrings)
            .orEmpty()
        val sheetPath = firstSheetPath(files)
        val rows = readRows(
            xml = files.getValue(sheetPath),
            sharedStrings = sharedStrings,
        )
        if (rows.isEmpty()) return LegacyImportPreview(records = emptyList(), errors = emptyList())

        val headers = rows.first().map { it.trim() }
        val data = rows.drop(1).map { row ->
            headers.mapIndexed { index, header ->
                header to row.getOrElse(index) { "" }
            }.toMap()
        }

        return LegacyMoneyManagerParser.parseRows(data)
    }

    private fun unzip(inputStream: InputStream): Map<String, ByteArray> {
        val files = mutableMapOf<String, ByteArray>()
        ZipInputStream(inputStream).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val currentEntry = entry
                if (!currentEntry.isDirectory) {
                    files[currentEntry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return files
    }

    private fun firstSheetPath(files: Map<String, ByteArray>): String {
        val workbook = parseXml(files.getValue("xl/workbook.xml"))
        val sheet = workbook.documentElement
            .elementsByTagName("sheet")
            .firstOrNull()
            ?: error("Workbook does not contain a sheet.")
        val relationshipId = sheet.getAttribute("r:id")
        val relationships = parseXml(files.getValue("xl/_rels/workbook.xml.rels"))
        val target = relationships.documentElement
            .elementsByTagName("Relationship")
            .firstOrNull { it.getAttribute("Id") == relationshipId }
            ?.getAttribute("Target")
            ?: error("Unable to resolve workbook sheet.")

        return when {
            target.startsWith("/") -> target.removePrefix("/")
            target.startsWith("xl/") -> target
            else -> "xl/$target"
        }
    }

    private fun readSharedStrings(xml: ByteArray): List<String> {
        val document = parseXml(xml)
        return document.documentElement
            .elementsByTagName("si")
            .map { stringItem ->
                stringItem.elementsByTagName("t").joinToString(separator = "") { it.textContent.orEmpty() }
            }
    }

    private fun readRows(xml: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val document = parseXml(xml)
        return document.documentElement
            .elementsByTagName("row")
            .map { row ->
                val values = mutableListOf<String>()
                row.elementsByTagName("c").forEach { cell ->
                    val index = columnIndex(cell.getAttribute("r"))
                    while (values.size <= index) values += ""
                    values[index] = cellValue(cell, sharedStrings)
                }
                values
            }
    }

    private fun cellValue(cell: Element, sharedStrings: List<String>): String {
        val type = cell.getAttribute("t")
        if (type == "inlineStr") {
            return cell.elementsByTagName("t").joinToString(separator = "") { it.textContent.orEmpty() }
        }

        val rawValue = cell.elementsByTagName("v").firstOrNull()?.textContent.orEmpty()
        return if (type == "s" && rawValue.isNotBlank()) {
            sharedStrings.getOrElse(rawValue.toInt()) { "" }
        } else {
            rawValue
        }
    }

    private fun columnIndex(cellReference: String): Int {
        val letters = cellReference.takeWhile { it.isLetter() }
        return letters.fold(0) { acc, char ->
            acc * 26 + (char.uppercaseChar() - 'A' + 1)
        } - 1
    }

    private fun parseXml(bytes: ByteArray) =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                isNamespaceAware = false
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            .newDocumentBuilder()
            .parse(bytes.inputStream())

    private fun Element.elementsByTagName(name: String): List<Element> {
        val nodes = getElementsByTagName(name)
        return buildList {
            repeat(nodes.length) { index ->
                (nodes.item(index) as? Element)?.let(::add)
            }
        }
    }

}
