package org.balefrost.aoc2024.day08

import org.balefrost.aoc2024.StringBased2DMap
import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines

data class Input(val antennaLocations: Map<Char, Set<XY>>, val map: StringBased2DMap)

fun getInput(lines: List<String>): Input {
    val map = StringBased2DMap(lines)

    val antennaLocations = mutableMapOf<Char, MutableSet<XY>>()
    for (y in lines.indices) {
        for (x in lines[y].indices) {
            if (lines[y][x] != '.') {
                antennaLocations.getOrPut(lines[y][x], ::mutableSetOf).add(XY(x, y))
            }
        }
    }

    return Input(antennaLocations, map)
}

fun generateAntinodes(pos: XY, dir: XY, map: StringBased2DMap) = generateSequence(pos) {
    val newPos = it + dir
    if (newPos !in map) {
        null
    } else {
        newPos
    }
}

object Day08Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day08.txt")

        val (antennaLocations, dims) = getInput(lines)

        val antinodes = mutableSetOf<XY>()
        for (locs in antennaLocations.values) {
            for (loc1 in locs) {
                for (loc2 in locs) {
                    if (loc1 == loc2) {
                        continue
                    }

                    val antinode = loc2 + (loc2 - loc1)
                    if (antinode in dims) {
                        antinodes += antinode
                    }
                }
            }
        }
        println(antinodes.count())
    }
}

object Day08Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day08.txt")

        val (antennaLocations, dims) = getInput(lines)

        val antinodes = mutableSetOf<XY>()

        for (locs in antennaLocations.values) {
            for (loc1 in locs) {
                for (loc2 in locs) {
                    if (loc1 == loc2) {
                        continue
                    }

                    val dir = loc2 - loc1
                    antinodes += generateAntinodes(loc1, dir, dims)
                    antinodes += generateAntinodes(loc1, -dir, dims)
                }
            }
        }
        println(antinodes.count())
    }
}