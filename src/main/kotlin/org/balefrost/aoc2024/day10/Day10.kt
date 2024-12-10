package org.balefrost.aoc2024.day10

import org.balefrost.aoc2024.StringBased2DMap
import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines

class Input(input: List<String>) {
    val map = StringBased2DMap(input)

    val positions = map.positions

    operator fun contains(pos: XY): Boolean = map.contains(pos)

    fun getHeight(pos: XY) = when (val d = map[pos]) {
        '.' -> -1
        else -> d.digitToInt()
    }
}

fun findStart(input: Input): Sequence<XY> {
    return input.positions.filter { input.getHeight(it) == 0 }
}

fun findAdjacent(input: Input, pos: XY): Set<XY> {
    val currentHeight = input.getHeight(pos)
    if (currentHeight == 9) {
        return emptySet()
    }
    return pos.adjacent.filterTo(mutableSetOf()) { it in input && input.getHeight(it) == currentHeight + 1 }
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
