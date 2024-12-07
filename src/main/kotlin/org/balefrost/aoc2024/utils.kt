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
    return sequence<T> {
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
                        val cycleItems = listOf(firstDep) + stack.asReversed().map { it[0] }.takeWhile { it != firstDep } + listOf(firstDep)
                        throw IllegalArgumentException("Dependency cycle ${cycleItems.asReversed().joinToString(" -> ")}")
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
