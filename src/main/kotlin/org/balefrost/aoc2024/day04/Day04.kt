package org.balefrost.aoc2024.day04

import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines

val dirs = (-1..1).flatMap { x ->
    (-1..1).map { y ->
        XY(x, y)
    }.filter { (x, y) -> x != 0 || y != 0 }
}

fun checkString(x: Int, y: Int, dx: Int, dy: Int, lines: List<String>, target: String): Boolean {
    if (target.isEmpty()) {
        return true
    }
    if (y < 0 || y > lines.lastIndex || x < 0 || x > lines[0].lastIndex) {
        return false
    }
    if (lines[y][x] != target[0]) {
        return false
    }
    return checkString(x + dx, y + dy, dx, dy, lines, target.substring(1))
}

object Day04Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day04.txt")
        var count = 0
        for (y in (0..lines.lastIndex)) {
            for (x in (0..lines[0].lastIndex)) {
                for ((dx, dy) in dirs) {
                    if (checkString(x, y, dx, dy, lines, "XMAS")) {
                        count += 1
                    }
                }
            }
        }
        println(count)
    }
}

object Day04Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day04.txt")
        fun checkX(x: Int, y: Int): Boolean {
            return (checkString(x - 1, y - 1, 1, 1, lines, "MAS") ||
                    checkString(x + 1, y + 1, -1, -1, lines, "MAS")) &&
                    (checkString(x - 1, y + 1, 1, -1, lines, "MAS") ||
                            checkString(x + 1, y - 1, -1, 1, lines, "MAS"))

        }

        var count = 0
        for (y in (0..lines.lastIndex)) {
            for (x in (0..lines[0].lastIndex)) {
                if (checkX(x, y)) {
                    count += 1
                }
            }
        }
        println(count)
    }
}