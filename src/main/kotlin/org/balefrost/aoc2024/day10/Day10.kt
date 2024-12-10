package org.balefrost.aoc2024.day10

import org.balefrost.aoc2024.readInputLines

data class XY(val x: Int, val y: Int) {
    operator fun minus(other: XY): XY = XY(x - other.x, y - other.y)
    operator fun plus(other: XY): XY = XY(x + other.x, y + other.y)
    operator fun unaryMinus() = XY(-x, -y)
    val adjacent
        get() = listOf(
            this + XY(1, 0),
            this + XY(0, 1),
            this + XY(-1, 0),
            this + XY(0, -1)
        )
}

data class WH(val w: Int, val h: Int) {
    operator fun contains(xy: XY): Boolean {
        return xy.x in 0..<w && xy.y in 0..<h
    }
}

data class Input(val map: List<String>) {
    val bounds = WH(map[0].length, map.size)

    fun getHeight(pos: XY) = when (val d = map[pos.y][pos.x]) {
        '.' -> -1
        else -> d.digitToInt()
    }
}

fun findStart(input: Input): Set<XY> {
    return sequence {
        for (y in 0..<input.bounds.h) {
            for (x in 0..<input.bounds.w) {
                val pos = XY(x, y)
                if (input.getHeight(pos) == 0) {
                    yield(pos)
                }
            }
        }
    }.toSet()
}

fun findAdjacent(input: Input, pos: XY): Set<XY> {
    val currentHeight = input.getHeight(pos)
    if (currentHeight == 9) {
        return emptySet()
    }
    return pos.adjacent.filterTo(mutableSetOf()) { it in input.bounds && input.getHeight(it) == currentHeight + 1 }
}

fun walk(input: Input, pos: XY): Sequence<XY> {
    return sequence<XY> {
        val fringe = ArrayDeque<XY>()
        fringe.add(pos)
        while (fringe.isNotEmpty()) {
            val visited = fringe.removeFirst()
            yield(visited)
            fringe.addAll(findAdjacent(input, visited))
        }
    }.distinct()
}

fun findWalks(input: Input, pos: XY): Sequence<List<XY>> {
    return sequence {
        var paths = listOf(listOf(pos))
        while (paths.isNotEmpty()) {
            val newPaths = paths.flatMap { path ->
                findAdjacent(input, path.last()).map { path + it }
            }
            val (finished, ongoing) = newPaths.partition { input.getHeight(it.last()) == 9 }
            yieldAll(finished)
            paths = ongoing
        }
    }
}

object Day10Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day10.txt")
        val input = Input(lines)
        val result = findStart(input).sumOf { trailhead ->
            walk(input, trailhead).filter { pos -> input.getHeight(pos) == 9 }.count()
        }
        println(result)
    }
}

object Day10Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day10.txt")
        val input = Input(lines)
        val result = findStart(input).sumOf { trailhead ->
            findWalks(input, trailhead).count()
        }
        println(result)
    }
}
