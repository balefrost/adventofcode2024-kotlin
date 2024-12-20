package org.balefrost.aoc2024.day20

import org.balefrost.aoc2024.Map2D
import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.makeMutableMapFromLines
import org.balefrost.aoc2024.readInputLines
import java.util.*

data class DijkstraCell<T>(val state: T, val totalCost: Long, val prevState: T?)

// TODO: reconcile with other implementations
fun <T> dijkstra(init: T, getNextStates: (T) -> List<Pair<Long, T>>): Map<T, DijkstraCell<T>> {

    val cache = mutableMapOf(init to DijkstraCell<T>(init, 0L, null))
    val queue = PriorityQueue<Pair<Long, T>>(compareBy { it.first })
    queue.add(0L to init)

    while (queue.isNotEmpty()) {
        val (costSoFar, state) = queue.remove()
        for ((incrementalCost, nextState) in getNextStates(state)) {
            if (cache.putIfAbsent(nextState, DijkstraCell<T>(nextState, costSoFar + incrementalCost, state)) == null) {
                queue.add(costSoFar + incrementalCost to nextState)
            }
        }
    }

    return cache
}

fun <T : Any> reconstructPath(finalState: T, results: Map<T, DijkstraCell<T>>): Sequence<T> {
    return generateSequence(finalState) { results.getValue(it).prevState }
}

data class CheatData(val cheatStart: XY, val cheatEnd: XY, val distanceSaved: Long)

fun findCheats(map: Map2D, cheatDistance: Int): Sequence<CheatData> {
    val startPos = map.positions.first { map[it] == 'S' }
    val endPos = map.positions.first { map[it] == 'E' }

    val noCheatResults = dijkstra(startPos) { pos ->
        pos.adjacent.filter { map[it] != '#' }.map { 1L to it }.toList()
    }

    return reconstructPath(endPos, noCheatResults).flatMap { cheatStart ->
        val noCheatCostToStart = noCheatResults.getValue(cheatStart).totalCost
        cheatStart.allWithinDistance(cheatDistance).filter { map[it] != '#' }.map { cheatEnd ->
            val noCheatCostToEnd = noCheatResults.getValue(cheatEnd).totalCost
            val noCheatDistance = noCheatCostToEnd - noCheatCostToStart
            val distanceSaved = noCheatDistance - cheatStart.manhattanDistanceTo(cheatEnd)
            CheatData(cheatStart, cheatEnd, distanceSaved)
        }.filter { it.distanceSaved > 0 }
    }
}

object Day20Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day20.txt")

        val map = makeMutableMapFromLines(lines, '#')

        println(findCheats(map, 2).count { it.distanceSaved >= 100 })
    }
}

object Day20Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day20.txt")

        val map = makeMutableMapFromLines(lines, '#')

        println(findCheats(map, 20).count { it.distanceSaved >= 100 })
    }

}
