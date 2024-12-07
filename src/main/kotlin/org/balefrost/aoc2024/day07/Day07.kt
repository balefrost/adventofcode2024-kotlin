package org.balefrost.aoc2024.day07

import org.balefrost.aoc2024.readInputLines

data class Input(val testValue: Long, val numbers: List<Long>)

fun parseInput(lines: List<String>): List<Input> {
    return lines.map { line ->
        val (testValue, rest) = line.split(": ", limit = 2)
        val numbers = rest.split(" ")
        Input(testValue.toLong(), numbers.map(String::toLong))
    }
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

object Day07Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day07.txt")

        val input = parseInput(lines)
        val result = input.filter { inp ->
            val operatorSequences = cartesianProduct((0 until inp.numbers.size - 1).map { listOf('+', '*') })
            operatorSequences.any { ops ->
                val result = ops.zip(inp.numbers.drop(1)).fold(inp.numbers[0]) { acc, opNum ->
                    val (op, num) = opNum
                    if (op == '+') {
                        acc + num
                    } else {
                        acc * num
                    }
                }
                result == inp.testValue
            }
        }.sumOf { it.testValue }
        println(result)
    }
}

object Day07Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day07.txt")

        val input = parseInput(lines)
        val result = input.filter { inp ->
            val operatorSequences = cartesianProduct((0 until inp.numbers.size - 1).map { listOf("+", "*", "||") })
            operatorSequences.any { ops ->
                val result = ops.zip(inp.numbers.drop(1)).fold(inp.numbers[0]) { acc, opNum ->
                    val (op, num) = opNum
                    if (op == "+") {
                        acc + num
                    } else if (op == "*") {
                        acc * num
                    } else {
                        (acc.toString() + num.toString()).toLong()
                    }
                }
                result == inp.testValue
            }
        }.sumOf { it.testValue }
        println(result)
    }
}