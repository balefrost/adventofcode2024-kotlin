package org.balefrost.aoc2024.day05

import org.balefrost.aoc2024.readInputLines
import org.balefrost.aoc2024.sortPartiallyOrdered

data class Input(
    val orderMap: Map<Int, Set<Int>>,
    val pages: List<List<Int>>
)

fun getInput(lines: List<String>): Input {
    val orders = lines.takeWhile { it.isNotBlank() }
    val pageSequences = lines.drop(orders.size + 1).map {
        it.split(",").map(Integer::parseInt)
    }
    val orderRegex = """(\d+)\|(\d+)""".toRegex()
    val orderMap = orders.map {
        val m = orderRegex.matchEntire(it)!!
        m.groupValues[1].toInt() to m.groupValues[2].toInt()
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    return Input(
        orderMap,
        pageSequences
    )
}

fun isValid(orderMap: Map<Int, Set<Int>>, pages: List<Int>): Boolean {
    // we cal walk "pages", keeping track of items that should not occur
    val invalidPages = mutableSetOf<Int>()
    for (page in pages.asReversed()) {
        if (page in invalidPages) {
            return false
        }
        orderMap[page]?.forEach(invalidPages::add)
    }
    return true
}

object Day05Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day05.txt")
        val (orderMap, pageSequences) = getInput(lines)
        val validSequences = pageSequences.filter { isValid(orderMap, it) }
        println(validSequences.sumOf { it[it.size / 2] })
    }
}

object Day05Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day05.txt")
        val (orderMap, pageSequences) = getInput(lines)
        val invalidSequences = pageSequences.filter { !isValid(orderMap, it) }
        val reorderedSequences = invalidSequences.map { pages ->
            val pageSet = pages.toSet()
            sortPartiallyOrdered(pageSet) { page ->
                orderMap.getOrDefault(page, emptySet()).filter { it in pageSet }
            }.toList().asReversed()
        }
        println(reorderedSequences.sumOf { it[it.size / 2] })
    }
}