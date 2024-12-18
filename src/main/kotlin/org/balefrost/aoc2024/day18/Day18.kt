package org.balefrost.aoc2024.day18

import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.day16.dijkstra
import org.balefrost.aoc2024.makeMutableMap
import org.balefrost.aoc2024.readInputLines

fun parseInput(lines: Iterable<String>): List<XY> {
    return lines.map {
        val (x, y) = it.split(",").map(String::toInt)
        XY(x, y)
    }
}

object Day18Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day18.txt")
        val positions = parseInput(lines)
        val map = makeMutableMap(71, 71, '.', '#')

        for (pos in positions.take(1024)) {
            map[pos] = '#'
        }

        val distanceMap = dijkstra(XY(0, 0)) { pos ->
            pos.adjacent.filter { map[it] == '.' }.map { 1L to it }.toList()
        }

        println(distanceMap[XY(70, 70)])
    }
}

object Day18Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day18.txt")
        val positions = parseInput(lines)
        val map = makeMutableMap(71, 71, '.', '#')

        val badPos = positions.first { dropPos ->
            map[dropPos] = '#'
            val distanceMap =
                dijkstra(XY(0, 0)) { pos -> pos.adjacent.filter { map[it] == '.' }.map { 1L to it }.toList() }
            distanceMap[XY(70, 70)] == null
        }

        println("${badPos.x},${badPos.y}")
    }
}
