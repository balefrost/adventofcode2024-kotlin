package org.balefrost.aoc2024

import java.io.InputStreamReader

fun readInputFile(filename: String) =
    InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)!!).use { it.readText() }

fun readInputLines(filename: String): List<String> {
    val allLines = readInputFile(filename).lines()
    val lastRelevantIndex = (allLines.lastIndex downTo 0).firstOrNull { allLines[it].isNotBlank() }
    if (lastRelevantIndex == null) {
        return emptyList()
    }
    return allLines.subList(0, lastRelevantIndex + 1)
}

fun <T> sortPartiallyOrdered(items: Iterable<T>, getDeps: (T) -> Iterable<T>): Iterable<T> {
    return sequence {
        val emitted = mutableSetOf<T>()
        for (firstItem in items) {
            val onStack = mutableSetOf(firstItem)

            // invariant: contains 0 or more deques, each of which contains at least 1 item.
            val stack = mutableListOf(ArrayDeque<T>().also { it.add(firstItem) })

            fun removeTopOfStack(): T {
                val removedItem = stack.last().removeFirst()
                onStack -= removedItem
                if (stack.last().isEmpty()) {
                    stack.removeLast()
                } else {
                    onStack += stack.last().first()
                }
                return removedItem
            }

            fun addToTopOfStack(items: Iterable<T>): Boolean {
                val deps = ArrayDeque<T>()
                for (item in items) {
                    if (item in emitted) {
                        continue
                    }
                    deps.addLast(item)
                }

                if (deps.isNotEmpty()) {
                    val firstDep = deps.first()
                    if (!onStack.add(firstDep)) {
                        val cycleItems =
                            listOf(firstDep) + stack.asReversed().map { it[0] }.takeWhile { it != firstDep } + listOf(
                                firstDep
                            )
                        throw IllegalArgumentException(
                            "Dependency cycle ${
                                cycleItems.asReversed().joinToString(" -> ")
                            }"
                        )
                    }
                    stack.addLast(deps)
                    return true
                }

                return false
            }

            while (stack.isNotEmpty()) {
                val item = stack.last().first()
                if (emitted.contains(item)) {
                    removeTopOfStack()
                    continue
                }
                if (!addToTopOfStack(getDeps(item))) {
                    removeTopOfStack()
                    emitted += item
                    yield(item)
                }
            }
        }
    }.asIterable()
}

fun <T> binarySearch(items: List<T>, comparison: (T) -> Int): Int {
    var low = 0
    var high = items.size
    while (low < high) {
        val mid = low + (high - low) / 2
        val midItem = items[mid]
        val comp = comparison(midItem)
        if (comp < 0) {
            high = mid
        } else if (comp > 0) {
            low = mid + 1
        } else {
            return mid
        }
    }
    return low.inv()
}

fun <T> cartesianProduct(items: List<List<T>>): Sequence<List<T>> {
    if (items.isEmpty()) {
        return sequenceOf(emptyList())
    }

    return sequence {
        for (item in items.first()) {
            for (tail in cartesianProduct(items.subList(1, items.size))) {
                yield(listOf(item) + tail)
            }
        }
    }
}

/**
 * Left-handed XY cartesian point
 */
data class XY(val x: Int, val y: Int) {
    operator fun minus(other: XY): XY = XY(x - other.x, y - other.y)
    operator fun plus(other: XY): XY = XY(x + other.x, y + other.y)
    operator fun unaryMinus() = XY(-x, -y)
    fun turnRight(): XY {
        return XY(-y, x)
    }
    val adjacent
        get() = listOf(
            this + XY(1, 0),
            this + XY(0, 1),
            this + XY(-1, 0),
            this + XY(0, -1)
        )
}

class StringBased2DMap(val lines: List<String>) {
    operator fun contains(pos: XY): Boolean = pos.y in lines.indices && pos.x in lines[pos.y].indices

    operator fun get(pos: XY) = lines[pos.y][pos.x]

    val positions get() = sequence {
        for (y in lines.indices) {
            for (x in lines[y].indices) {
                yield(XY(x, y))
            }
        }
    }
}


