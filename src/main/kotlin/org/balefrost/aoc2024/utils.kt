package org.balefrost.aoc2024

import java.io.InputStreamReader

fun readInputFile(filename: String) =
    InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)!!).use { it.readText() }

fun readInputLines(filename: String): List<String> {
    val allLines = readInputFile(filename).lines()
    val lastRelevantIndex = (allLines.lastIndex downTo 0).firstOrNull { allLines[it].isNotBlank() }
    if (lastRelevantIndex == null) {
        return emptyList()
    }
    return allLines.subList(0, lastRelevantIndex + 1)
}