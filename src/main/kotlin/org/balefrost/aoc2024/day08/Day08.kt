package org.balefrost.aoc2024.day08

import org.balefrost.aoc2024.readInputLines

data class XY(val x: Int, val y: Int) {
    operator fun minus(other: XY): XY = XY(x - other.x, y - other.y)
    operator fun plus(other: XY): XY = XY(x + other.x, y + other.y)
    operator fun unaryMinus() = XY(-x, -y)
}

data class WH(val w: Int, val h: Int) {
    operator fun contains(xy: XY): Boolean {
        return xy.x in 0..<w && xy.y in 0..<h
    }
}

data class Input(val antennaLocations: Map<Char, Set<XY>>, val dims: WH)

fun getInput(lines: List<String>): Input {
    val dims = WH(lines[0].length, lines.size)

    val antennaLocations = mutableMapOf<Char, MutableSet<XY>>()
    for (y in lines.indices) {
        for (x in lines[y].indices) {
            if (lines[y][x] != '.') {
                antennaLocations.getOrPut(lines[y][x], ::mutableSetOf).add(XY(x, y))
            }
        }
    }

    return Input(antennaLocations, dims)
}

fun generateAntinodes(pos: XY, dir: XY, dims: WH) = generateSequence(pos) {
    val newPos = it + dir
    if (newPos !in dims) {
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