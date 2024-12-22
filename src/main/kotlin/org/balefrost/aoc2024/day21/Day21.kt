package org.balefrost.aoc2024.day21

import org.balefrost.aoc2024.*
import kotlin.math.abs

// Generally, a path ^^^>>> makes more sense than a path ^>^>^>. The latter will require the robot manning the
// parent remote control to need to make more moves.
fun findOrthoPaths(start: XY, end: XY): Sequence<Sequence<XY>> {
    val diff = end - start
    val dir = diff.sign
    return sequence {
        yield(sequence {
            var xx = start.x
            var yy = start.y
            yield(start)
            repeat(abs(diff.x)) {
                xx += dir.x
                yield(XY(xx, yy))
            }
            repeat(abs(diff.y)) {
                yy += dir.y
                yield(XY(xx, yy))
            }
        })
        if (abs(diff.x) > 0 && abs(diff.y) > 0) {
            yield(sequence {
                var xx = start.x
                var yy = start.y
                yield(start)
                repeat(abs(diff.y)) {
                    yy += dir.y
                    yield(XY(xx, yy))
                }
                repeat(abs(diff.x)) {
                    xx += dir.x
                    yield(XY(xx, yy))
                }
            })
        }
    }
}

fun pathToDirectionSymbols(path: Iterable<XY>): Iterable<Char> {
    return path.zipWithNext { a, b ->
        val dir = (b - a).sign
        when (dir) {
            XY(0, 1) -> 'v'
            XY(0, -1) -> '^'
            XY(1, 0) -> '>'
            XY(-1, 0) -> '<'
            else -> error("invalid direction")
        }
    }
}

fun pathToDirectionSymbols(path: Sequence<XY>): Sequence<Char> {
    return pathToDirectionSymbols(path.asIterable()).asSequence()
}

fun computeComplexity(str: String, stack: List<Map2D>): Long {
    data class CacheKey(val buttonSequence: String, val complexityLevel: Int)
    class StackEntryData(val symbolToPos: Map<Char, XY>, val grid: Map2D)

    val stackEntries = stack.map { grid ->
        val lookup = grid.positions.associateBy { grid[it] }
        StackEntryData(
            lookup,
            grid
        )
    }
    val cache = mutableMapOf<CacheKey, Long>()
    fun helper(str: String, complexityLevel: Int): Long {
        check(complexityLevel >= 0)
        val key = CacheKey(str, complexityLevel)
        val existing = cache[key]
        if (existing != null) {
            return existing
        }
        if (complexityLevel == stackEntries.size) {
            val result = str.length.toLong()
            cache[key] = result
            return result
        }
        val stackEntry = stackEntries[complexityLevel]
        val motions = ("A$str").map { stackEntry.symbolToPos.getValue(it) }.zipWithNext()
        val pathSegments = motions.map { (start, end) ->
            val allPathsForMotion = findOrthoPaths(start, end)
                .filter { path -> path.none { stackEntry.grid[it] == '#' } }
                .map { pathToDirectionSymbols(it).joinToString("") + "A" }
            allPathsForMotion
                .map { path -> helper(path, complexityLevel + 1) }
                .min()
        }
        val totalLength = pathSegments.sum()
        cache[key] = totalLength
        return totalLength
    }

    val length = helper(str, 0)
    val numericPart = str.dropWhile { it == '0' }.dropLast(1).toLong()
    return length * numericPart
}

val keypadGrid = makeMutableMapFromLines(
    """
            789
            456
            123
            #0A
        """.trimIndent().lines(), '#'
)

val remoteGrid = makeMutableMapFromLines(
    """
                #^A
                <v>
            """.trimIndent().lines(), '#'
)


object Day21Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day21.txt")

        println(lines.sumOf { line ->
            computeComplexity(line, listOf(keypadGrid) + (0..<2).map { remoteGrid })
        })
    }
}

object Day21Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day21.txt")

        println(lines.sumOf { line ->
            computeComplexity(line, listOf(keypadGrid) + (0..<25).map { remoteGrid })
        })
    }
}
