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

fun sortPartiallyOrdered(pages: Iterable<Int>, getDeps: (Int) -> Iterable<Int>): Iterable<Int> {
    return sequence<Int> {
        val emitted = mutableSetOf<Int>()
        for (page in pages) {
            val onStack = mutableSetOf(page)
            val stack = mutableListOf(ArrayDeque<Int>().also { it.add(page) })
            while (stack.isNotEmpty()) {
                if (stack.last().isEmpty()) {
                    stack.removeLast()
                    continue
                }

                val item = stack.last().first()
                if (emitted.contains(item)) {
                    stack.last().removeFirst()
                    onStack -= item
                    continue
                }
                val deps = ArrayDeque<Int>()
                for (dep in getDeps(item)) {
                    if (dep in emitted) {
                        continue
                    }
                    if (!onStack.add(dep)) {
                        val cycleItems = listOf(dep) + stack.asReversed().map { it[0] }.takeWhile { it != dep } + listOf(dep)
                        throw IllegalArgumentException("Dependency cycle ${cycleItems.asReversed().joinToString(" -> ")}")
                    }
                    deps.addLast(dep)
                }
                if (deps.isEmpty()) {
                    stack.last().removeFirst()
                    onStack -= item
                    emitted += item
                    yield(item)
                } else {
                    stack.add(deps)
                }
            }
        }
    }.asIterable()
}
