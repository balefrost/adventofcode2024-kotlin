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

fun evaluateNumbers(numbers: List<Long>, target: Long, possibleOperators: List<(Long, Long) -> Long>): Boolean {
    fun helper(acc: Long, numbers: List<Long>, target: Long, possibleOperators: List<(Long, Long) -> Long>): Boolean {
        if (acc > target) {
            return false
        }

        if (numbers.isEmpty()) {
            return acc == target
        }

        for (operator in possibleOperators) {
            if (helper(operator(acc, numbers[0]), numbers.subList(1, numbers.size), target, possibleOperators)) {
                return true
            }
        }
        return false
    }

    return helper(numbers[0], numbers.drop(1), target, possibleOperators)
}

object Day07Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day07.txt")

        val input = parseInput(lines)
        val result = input.filter { inp ->
            evaluateNumbers(inp.numbers, inp.testValue, listOf(
                { acc: Long, num: Long -> acc + num },
                { acc: Long, num: Long -> acc * num }
            ))
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
            evaluateNumbers(inp.numbers, inp.testValue, listOf(
                { acc: Long, num: Long -> acc + num },
                { acc: Long, num: Long -> acc * num },
                { acc: Long, num: Long -> "$acc$num".toLong() }
            ))
        }.sumOf { it.testValue }
        println(result)
    }
}