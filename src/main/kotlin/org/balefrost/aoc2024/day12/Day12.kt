package org.balefrost.aoc2024.day12

import org.balefrost.aoc2024.StringBased2DMap
import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

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

suspend fun <R, T> SequenceScope<T>.srun(block: suspend SequenceScope<T>.() -> R): R {
    return block()
}

object Day12Part01Vis {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day12.txt")

        val grid = StringBased2DMap(lines, '.')
        val plots = findPlots(grid)

        val plotAdjacency = plots.associateWith { plot ->
            val allAdjacent =
                plot.items.asSequence().flatMap { it.adjacent }.distinct().filterNot { it in plot.items }.toSet()
            plots.filter { it != plot && it.items.intersect(allAdjacent).isNotEmpty() }
        }

        var numColors = 0
        val plotColorIndices = mutableMapOf<Plot, Int>()
        for (plot in plots) {
            val adjacentPlots = plotAdjacency.getValue(plot)
            val adjacentColorIndices = adjacentPlots.mapNotNull(plotColorIndices::get).toSet()
            var colorToUse = (0..<numColors).firstOrNull { it !in adjacentColorIndices }
            if (colorToUse == null) {
                colorToUse = numColors++
            }
            plotColorIndices[plot] = colorToUse
        }

        val colors = (0..<numColors).map { Color.getHSBColor(it.toFloat() / numColors, 1.0f, 1.0f) }.toTypedArray()

        val positionToColor = plotColorIndices.flatMap { (plot, idx) ->
            plot.items.map {
                it to colors[idx]
            }
        }.toMap()

        data class GraphicsOp(val pos: XY, val op: (JLabel) -> Unit)

        val steps = sequence {
            val finished = mutableSetOf<XY>()
            var previousTrailhead: XY? = null

            for (position in grid.positions) {
                if (!finished.add(position)) {
                    continue
                }
                val saturatedColor = positionToColor.getValue(position)
                val dimColor = saturatedColor.darker()

                srun {
                    val changeOps = mutableListOf<GraphicsOp>()
                    if (previousTrailhead != null) {
                        changeOps += GraphicsOp(previousTrailhead!!) {
                            it.border = BorderFactory.createLineBorder(Color(0xff, 0xff, 0xff, 0x00), 1)
                        }
                    }
                    changeOps += GraphicsOp(position) {
                        it.border = BorderFactory.createLineBorder(Color.BLACK, 1)
                    }
                    yield(changeOps)
                }

                val type = grid[position]
                val plot = mutableSetOf<XY>()
                var more = setOf(position)
                while (more.isNotEmpty()) {
                    yield(more.map { xy ->
                        GraphicsOp(xy) {
                            it.background = dimColor
                        }
                    })
                    plot += more
                    more = more.asSequence().flatMap { m -> m.adjacent }.distinct()
                        .filter { it in grid && it !in plot && grid[it] == type }.toSet()
                }
                yield(plot.map { xy ->
                    GraphicsOp(xy) { it.background = saturatedColor }
                })
                finished.addAll(plot)

                previousTrailhead = position
            }
        }

        val stepsIter = steps.iterator()
        val f = JDialog()
        val labels = mutableMapOf<XY, JLabel>()
        fun takeStep() {
            if (stepsIter.hasNext()) {
                val ops = stepsIter.next()
                for (op in ops) {
                    labels[op.pos]?.let { op.op(it) }
                }
            }
        }
        f.contentPane.layout = BorderLayout()
        val gridPanel = JPanel()
        val gridScroll =
            JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
        f.contentPane.add(gridScroll, BorderLayout.CENTER)
        gridPanel.layout = GridLayout(grid.dims.w, grid.dims.h)
        for (y in 0..<grid.dims.h) {
            for (x in 0..<grid.dims.w) {
                val pos = XY(x, y)
                val l = JLabel("${grid[pos]}").apply {
                    isOpaque = true
                    maximumSize = Dimension(10, 10)
                    minimumSize = Dimension(10, 10)
                    size = Dimension(10, 10)
                    border = BorderFactory.createLineBorder(Color(0xff, 0xff, 0xff, 0x00), 1)
                }
                labels[pos] = l
                gridPanel.add(l)
            }
        }
        Timer(2) { takeStep() }.start()
        f.pack()
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.isVisible = true
    }
}
