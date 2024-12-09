package org.balefrost.aoc2024.day09

import org.balefrost.aoc2024.readInputFile

data class FileAndRange(val fileId: Int, val range: IntRange)

fun parseInput(input: String): Sequence<FileAndRange> {
    return sequence {
        var i = 0
        var index = 0
        var fileId = 0
        var end: Int
        while (i < input.length) {
            val occupied = input[i].digitToInt()
            check(occupied != 0)
            end = index + occupied
            yield(FileAndRange(fileId, index until end))
            index = end
            ++fileId
            ++i

            if (i >= input.length) {
                break
            }

            val free = input[i].digitToInt()
            if (free != 0) {
                end = index + free
                yield(FileAndRange(-1, index until end))
                index = end
            }
            ++i
        }
    }
}

object Day09Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day09.txt").trim()
        val content = parseInput(input).flatMap { (fileId, indices) ->
            indices.map { fileId }
        }.toMutableList()

        var free = 0
        var end = content.lastIndex

        fun advanceFree() {
            while (free < content.size && content[free] != -1) {
                ++free
            }
        }

        fun advanceEnd() {
            while (end >= 0 && content[end] == -1) {
                --end
            }
        }

        advanceFree()
        advanceEnd()

        while (end > free) {
            content[free] = content[end]
            content[end] = -1
            advanceFree()
            advanceEnd()
        }

        println(content.takeWhile { it != -1 }.mapIndexed { index, value -> index.toLong() * value }.sum())
    }
}

fun compactFiles(occupiedRanges: List<FileAndRange>, freeRanges: List<IntRange>): List<FileAndRange> {
    val remainingFreeRanges = freeRanges.toMutableList()

    val finalOccupied = mutableListOf<FileAndRange>()
    for (fileAndRange in occupiedRanges.asReversed()) {
        val (fileId, originalRange) = fileAndRange
        val freeRangeIdx = remainingFreeRanges.asSequence().takeWhile { it.first <= originalRange.first }
            .indexOfFirst { it.count() >= originalRange.count() }
        if (freeRangeIdx == -1) {
            finalOccupied += fileAndRange
            continue
        }
        val freeRange = remainingFreeRanges[freeRangeIdx]

        val splitRanges = freeRange.splitAt(freeRange.first + originalRange.count())
        finalOccupied += FileAndRange(fileId, splitRanges[0])
        if (splitRanges.size > 1) {
            remainingFreeRanges[freeRangeIdx] = splitRanges[1]
        } else {
            remainingFreeRanges.removeAt(freeRangeIdx)
        }
    }

    return finalOccupied
}

object Day09Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day09.txt").trim()
        val occupiedRanges = mutableListOf<FileAndRange>()
        val freeRanges = mutableListOf<IntRange>()
        for (fileAndRange in parseInput(input)) {
            if (fileAndRange.fileId == -1) {
                freeRanges.add(fileAndRange.range)
            } else {
                occupiedRanges.add(fileAndRange)
            }
        }

        val finalOccupiedRanges = compactFiles(occupiedRanges, freeRanges)

        val checksum = finalOccupiedRanges.sumOf { (fileId, occupiedRange) ->
            occupiedRange.sumOf { it.toLong() * fileId }
        }

        println(checksum)
    }
}

fun IntRange.splitAt(index: Int): List<IntRange> {
    return when (index) {
        this.first -> listOf(this)
        this.last + 1 -> listOf(this)
        else -> listOf(this.first..<index, index..this.last)
    }
}