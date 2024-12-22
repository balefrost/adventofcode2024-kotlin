package org.balefrost.aoc2024.day22

import org.balefrost.aoc2024.readInputLines
import kotlin.time.measureTimedValue

fun secretNumbers(init: Long): Sequence<Long> = generateSequence(init) {
    var n = it
    n = ((n * 64) xor n) % 16777216
    n = ((n / 32) xor n) % 16777216
    n = ((n * 2048) xor n) % 16777216
    n
}

object Day22Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day22.txt")

        lines.sumOf { secretNumbers(it.toLong()).drop(2000).first() }.also(::println)
    }
}

// This was originally a clever value class that packed 4 ints in the range -9..9 into a single Int. My thinking was
// that comparing such ints would be faster than comparing separate components.
//
// For reasons that I cannot fathom, that was significantly slower than this approach.
data class PackedPriceChange(val a: Int, val b: Int, val c: Int, val d: Int) {

    override fun toString(): String {
        return "[$a, $b, $c, $d]"
    }

    companion object {
        fun pack(a: Int, b: Int, c: Int, d: Int): PackedPriceChange {
            check(a in -9..9)
            check(b in -9..9)
            check(c in -9..9)
            check(d in -9..9)
            return PackedPriceChange(a, b, c, d)
        }
    }
}

object Day22Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day22.txt")

        val firstPrices = measureTimedValue {
            lines.asSequence().map { line ->
                val firstChanges = mutableMapOf<PackedPriceChange, Int>()
                secretNumbers(line.toLong()).take(2001).map { (it % 10).toInt() }.windowed(5).forEach { numbers ->
                    val (a, b, c, d) = numbers.zipWithNext { a, b -> b - a }
                    val final = numbers.last()
                    val packed = PackedPriceChange.pack(a, b, c, d)
                    firstChanges.putIfAbsent(packed, final)
                }
                firstChanges
            }.toList()
        }.also { println("gathered prices: ${it.duration}") }.value

        val allSequences = measureTimedValue {
            firstPrices.flatMap { it.keys }.toSet()
        }.also { println("found all sequences: ${it.duration}") }.value

        println("distinct sequences: ${allSequences.count()}")

        val max = measureTimedValue {
            allSequences.parallelStream().mapToInt { sequence ->
                firstPrices.sumOf { lookup ->
                    lookup.getOrDefault(sequence, 0)
                }
            }.max().asInt
        }.also { println("found max: ${it.duration}") }.value

        println(max)
    }
}
