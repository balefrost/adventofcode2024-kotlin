package org.balefrost.aoc2024.day19

import org.balefrost.aoc2024.readInputLines

data class Input(val towelPatterns: Set<String>, val desiredPatterns: List<String>)

fun parseInput(lines: List<String>): Input {
    val linesIter = lines.iterator()
    val towelPatterns = linesIter.next().split(", ").toSet()
    linesIter.next()
    val desiredPatterns = mutableListOf<String>()
    linesIter.forEachRemaining {
        desiredPatterns += it
    }
    return Input(towelPatterns, desiredPatterns)
}

fun countArrangements(towelPatterns: Set<String>, desiredPattern: String, cache: MutableMap<String, Long>): Long {
    cache[""] = 1L

    fun helper(remainingDesiredPattern: String): Long {
        val existing = cache[remainingDesiredPattern]
        if (existing != null) {
            return existing
        }

        val result = towelPatterns.sumOf { towelPattern ->
            if (towelPattern.commonPrefixWith(remainingDesiredPattern) == towelPattern) {
                helper(remainingDesiredPattern.substring(towelPattern.length))
            } else {
                0L
            }
        }

        cache[remainingDesiredPattern] = result
        return result
    }
    return helper(desiredPattern)
}


object Day19Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day19.txt")
        val (towelPatterns, desiredPatterns) = parseInput(lines)

        val cache = mutableMapOf<String, Long>()
        val possible = desiredPatterns.count { desiredPattern ->
            countArrangements(towelPatterns, desiredPattern, cache) > 0
        }

        println(possible)
    }
}

object Day19Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day19.txt")
        val (towelPatterns, desiredPatterns) = parseInput(lines)

        val cache = mutableMapOf("" to 1L)
        val possible = desiredPatterns.sumOf { desiredPattern ->
            countArrangements(towelPatterns, desiredPattern, cache)
        }

        println(possible)
    }
}
