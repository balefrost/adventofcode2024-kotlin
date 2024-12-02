package org.balefrost.aoc2024.day02

import org.balefrost.aoc2024.readInputLines
import kotlin.math.abs

fun isSafeReport(report: List<Int>): Boolean {
    val diffs = report.zipWithNext().map { (a, b) -> b - a }
    val allValidIncreasing = diffs.all { abs(it) in 1 .. 3  && it > 0 }
    val allValidDecreasing = diffs.all { abs(it) in 1 .. 3  && it < 0 }
    return allValidIncreasing || allValidDecreasing
}

object Day02Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val reports = readInputLines("inputs/day02.txt").map { it.split(" +".toRegex()).map(Integer::parseInt) }
        val safeReports = reports.filter(::isSafeReport)
        println(safeReports.size)
    }
}

object Day02Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val reports = readInputLines("inputs/day02.txt").map { it.split(" +".toRegex()).map(Integer::parseInt) }

        fun isSafeReportWithDampening(report: List<Int>): Boolean {
            return isSafeReport(report) || report.indices.any { idx ->
                isSafeReport(report.subList(0, idx) + report.subList(idx + 1, report.size))
            }
        }

        val safeReports = reports.filter(::isSafeReportWithDampening)
        println(safeReports.size)
    }
}