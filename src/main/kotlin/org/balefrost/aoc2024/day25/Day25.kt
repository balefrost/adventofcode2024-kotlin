package org.balefrost.aoc2024.day25

import org.balefrost.aoc2024.readInputLines

data class Input(val locks: List<List<Int>>, val keys: List<List<Int>>)

fun parseInput(lines: Iterable<String>): Input {
    val locks = mutableListOf<List<Int>>()
    val keys = mutableListOf<List<Int>>()
    val iter = lines.iterator()
    while (iter.hasNext()) {
        val rows = mutableListOf<String>()
        for (line in iter) {
            if (line.isBlank()) {
                break
            }
            rows.add(line)
        }
        check(rows.size >= 2)
        check(rows.all { it.length == rows[0].length })
        val counts = rows[0].indices.map { c ->
            rows.drop(1).dropLast(1).count { it[c] == '#' }
        }
        if (rows[0][0] == '#') {
            locks.add(counts)
        } else {
            keys.add(counts)
        }
    }

    return Input(locks, keys)
}

object Day25Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day25.txt")

        val (locks, keys) = parseInput(lines)

        var good = 0
        for (lock in locks) {
            for (key in keys) {
                if (lock.zip(key).all { (a, b) -> a + b <= 5 }) {
                    ++good
                }
            }
        }
        println(good)
    }
}
