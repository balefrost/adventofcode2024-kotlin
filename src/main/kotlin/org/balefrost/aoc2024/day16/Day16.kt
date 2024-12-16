package org.balefrost.aoc2024.day16

import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.makeMutableMapFromLines
import org.balefrost.aoc2024.readInputLines
import java.util.PriorityQueue

fun <T> dijkstra(init: T, getNextStates: (T) -> List<Pair<Long, T>>): Map<T, Long> {

    val cache = mutableMapOf(init to 0L)
    val queue = PriorityQueue<Pair<Long, T>>(compareBy { it.first })
    queue.add(0L to init)

    while (queue.isNotEmpty()) {
        val (costSoFar, state) = queue.remove()
        for ((incrementalCost, nextState) in getNextStates(state)) {
            if (cache.putIfAbsent(nextState, costSoFar + incrementalCost) == null) {
                queue.add(costSoFar + incrementalCost to nextState)
            }
        }
    }

    return cache
}

object Day16Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day16.txt")
        val map = makeMutableMapFromLines(lines, '#')
        val initialPos = map.positions.first { map[it] == 'S' }
        val initialDir = XY(1, 0)

        data class State(val pos: XY, val dir: XY) {
            fun turnLeft() = State(pos, dir.turnLeft())
            fun turnRight() = State(pos, dir.turnRight())
            fun moveForward() = State(forward, dir)
            val forward get() = pos + dir
        }

        val initialState = State(initialPos, initialDir)
        val finalPos = map.positions.first { map[it] == 'E' }


        val results = dijkstra(initialState) { state ->
            listOf(
                1000L to state.turnLeft(),
                1000L to state.turnRight(),
            ) + if (map[state.forward] in setOf('S', 'E', '.')) {
                listOf(1L to state.moveForward())
            } else {
                emptyList()
            }
        }

        println(results.filter { it.key.pos == finalPos }.minBy { it.value }.value)
    }
}

interface FullDijkstraCell<T> {
    val cost: Long
    val paths: List<List<T>>
}

fun <T> fullDijkstra(init: T, getNextStates: (T) -> List<Pair<Long, T>>): Map<T, FullDijkstraCell<T>> {
    data class FullDijkstraCellImpl<T>(override val cost: Long, override val paths: List<List<T>>) : FullDijkstraCell<T>

    val cache = mutableMapOf(init to FullDijkstraCellImpl(0L, listOf(listOf(init))))
    val queue = PriorityQueue<Pair<Long, List<T>>>(compareBy { it.first })
    queue.add(0L to listOf(init))

    while (queue.isNotEmpty()) {
        val (costSoFar, pathSoFar) = queue.remove()
        val existing = cache[pathSoFar.last()]
        if (existing != null) {
            check(costSoFar >= existing.cost) { "somehow, cost dropped from ${existing.cost} to $costSoFar" }
            if (costSoFar > existing.cost) {
                continue
            }
            cache[pathSoFar.last()] = existing.copy(paths = existing.paths + listOf(pathSoFar))
        } else {
            cache[pathSoFar.last()] = FullDijkstraCellImpl(costSoFar, listOf(pathSoFar))
        }

        for ((incrementalCost, nextState) in getNextStates(pathSoFar.last())) {
            queue.add(costSoFar + incrementalCost to pathSoFar + nextState)
        }
    }

    return cache
}


object Day16Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day16.txt")
        val map = makeMutableMapFromLines(lines, '#')
        val initialPos = map.positions.first { map[it] == 'S' }
        val initialDir = XY(1, 0)

        data class State(val pos: XY, val dir: XY) {
            fun turnLeft() = State(pos, dir.turnLeft())
            fun turnRight() = State(pos, dir.turnRight())
            fun moveForward() = State(forward, dir)
            val forward get() = pos + dir
        }

        val initialState = State(initialPos, initialDir)
        val finalPos = map.positions.first { map[it] == 'E' }

        fun getNextStates(state: State): List<Pair<Long, State>> {
            return listOf(
                1000L to state.turnLeft(),
                1000L to state.turnRight(),
            ) + if (map[state.forward] in setOf('S', 'E', '.')) {
                listOf(1L to state.moveForward())
            } else {
                emptyList()
            }
        }

        val results = fullDijkstra(initialState, ::getNextStates)
        val pathsToEnd = results.filter { it.key.pos == finalPos }.values.sortedBy { it.cost }
        val bestPaths = pathsToEnd.filter { it.cost == pathsToEnd[0].cost }
        val bestLocations = bestPaths.flatMap { it.paths.flatten() }.map { it.pos }.toSet()
        println(bestLocations.size)
    }
}
