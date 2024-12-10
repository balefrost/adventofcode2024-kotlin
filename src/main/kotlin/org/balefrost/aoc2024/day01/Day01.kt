package org.balefrost.aoc2024.day01

import org.balefrost.aoc2024.readInputLines
import kotlin.math.abs

data class NumberLists(
    val lefts: List<Int>,
    val rights: List<Int>
)

fun getNumbers(): NumberLists {
    val numbers = readInputLines("inputs/day01.txt").map { it.split(" +".toRegex()).map(Integer::parseInt) }
    val lefts = numbers.map { it[0] }
    val rights = numbers.map { it[1] }
    return NumberLists(lefts, rights)
}

object Day01Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val (lefts, rights) = getNumbers()
        val totalDistance = lefts.sorted().zip(rights.sorted()).sumOf { (left, right) ->
            abs(left - right)
        }
        println(totalDistance)
    }
}

object Day01Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val (lefts, rights) = getNumbers()
        val rightLookup = rights.groupBy { it }.mapValues { it.value.size }
        println(lefts.sumOf { left -> left * rightLookup.getOrDefault(left, 0) })
    }
}