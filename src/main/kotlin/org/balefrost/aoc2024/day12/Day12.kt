package org.balefrost.aoc2024.day12

import org.balefrost.aoc2024.StringBased2DMap
import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines
import java.awt.*
import javax.swing.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

data class Plot(val type: Char, val items: Set<XY>)

private fun countSides(plot: Set<XY>): Int {
    var sides = 0
    for (pos in plot) {
        val p = pos.dirs8way.map { it in plot }
        // A position represents a potential side if one of its cardinal neighbors is missing.
        // A single position could represent up to 4 edges.
        // However, several adjacent positions could have this same missing neighbor,
        //   yet only one should count as the side
        // So a position is only considered as a side if it is the first (W->E, N->S) to have that property.
        if (!p.n && (p.w && p.nw || !p.w)) ++sides
        if (!p.s && (p.w && p.sw || !p.w)) ++sides
        if (!p.w && (p.n && p.nw || !p.n)) ++sides
        if (!p.e && (p.n && p.ne || !p.n)) ++sides
    }
    return sides
}

private fun findAdjacentWithSameType(grid: StringBased2DMap, position: XY): Set<XY> {
    val type = grid[position]
    val plot = mutableSetOf<XY>()
    var more = setOf(position)
    while (more.isNotEmpty()) {
        plot += more
        more = more.asSequence().flatMap { m -> m.adjacent }.distinct()
            .filter { it in grid && it !in plot && grid[it] == type }.toSet()
    }

    return plot
}

private fun findPlots(grid: StringBased2DMap): List<Plot> {
    val finished = mutableSetOf<XY>()
    val plots = mutableListOf<Plot>()

    for (position in grid.positions) {
        if (!finished.add(position)) {
            continue
        }

        val type = grid[position]
        val plot = findAdjacentWithSameType(grid, position)
        plots.add(Plot(type, plot))
        finished.addAll(plot)
    }

    return plots
}

object Day12Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day12.txt")

        val grid = StringBased2DMap(lines, '.')
        val plots = findPlots(grid)

        val cost = plots.sumOf { plot ->
            val area = plot.items.size
            val perimeter = plot.items.sumOf { pos -> pos.adjacent.count { grid[it] != plot.type } }
            area * perimeter
        }

        println(cost)
    }
}

object Day12Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day12.txt")

        val grid = StringBased2DMap(lines, '.')
        val plots = findPlots(grid)

        val cost = plots.sumOf { plot ->
            val area = plot.items.size
            val numSides = countSides(plot.items)
            area * numSides
        }

        println(cost)
    }
}
