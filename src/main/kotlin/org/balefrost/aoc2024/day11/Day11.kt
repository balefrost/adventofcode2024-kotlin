package org.balefrost.aoc2024.day11

import org.balefrost.aoc2024.readInputFile

fun blink(i: Long): List<Long> {
    if (i == 0L) {
        return listOf(1L)
    }
    val str = i.toString()
    if (str.length % 2 == 0) {
        return listOf(
            str.substring(0, str.length / 2).toLong(),
            str.substring(str.length / 2).toLong()
        )
    }
    return listOf(i * 2024L)
}

object Day11Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day11.txt").trim()
        var numbers = input.split(""" +""".toRegex()).map(String::toLong)

        repeat(25) {
            numbers = numbers.flatMap(::blink)
        }

        println(numbers.count())
    }
}

object Day11Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day11.txt").trim()
        val numbers = input.split(""" +""".toRegex()).map(String::toLong)
        var counts = numbers.groupBy { it }.mapValues { it.value.size.toLong() }

        repeat(75) {
            val newCounts = mutableMapOf<Long, Long>()
            for ((key, count) in counts) {
                for (newKey in blink(key)) {
                    newCounts.merge(newKey, count, Long::plus)
                }
            }
            counts = newCounts
        }

        println(counts.values.sum())
    }
}
