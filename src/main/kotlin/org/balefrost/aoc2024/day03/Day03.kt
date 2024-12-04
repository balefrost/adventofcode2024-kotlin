package org.balefrost.aoc2024.day03

import org.balefrost.aoc2024.readInputFile

object Day03Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day03.txt")
        val result = """mul\((\d+),(\d+)\)""".toRegex().findAll(input).sumOf {
            it.groupValues.drop(1).map(String::toInt).reduce(Int::times)
        }
        println(result)
    }
}

object Day03Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day03.txt")
        val mulRegex = """(mul)\((\d+),(\d+)\)""".toRegex()
        val doRegex = """(do)\(\)""".toRegex()
        val dontRegex = """(don't)\(\)""".toRegex()

        val matchesInOrder =
            (mulRegex.findAll(input) + doRegex.findAll(input) + dontRegex.findAll(input)).sortedBy { it.range.first }
                .toList()
        var enabled = true
        var sum = 0L
        for (matchResult in matchesInOrder) {
            when (matchResult.groupValues[1]) {
                "do" -> enabled = true
                "don't" -> enabled = false
                "mul" -> if (enabled) {
                    sum += matchResult.groupValues[2].toLong() * matchResult.groupValues[3].toLong()
                }
            }
        }
        println(sum)
    }
}