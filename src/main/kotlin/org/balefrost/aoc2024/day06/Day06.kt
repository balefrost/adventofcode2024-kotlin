package org.balefrost.aoc2024.day06

import org.balefrost.aoc2024.XY
import org.balefrost.aoc2024.readInputLines

data class PosDir(val pos: XY, val dir: XY) {
    fun turnRight(): PosDir {
        return PosDir(pos, dir.turnRight())
    }

    fun step(): PosDir {
        return PosDir(pos + dir, dir)
    }
}

fun findStartingPos(map: List<String>): XY {
    for (y in map.indices) {
        for (x in map[y].indices) {
            if (map[y][x] == '^') {
                return XY(x, y)
            }
        }
    }
    throw IllegalArgumentException()
}

enum class WalkTermination { OffEdge, Loop }
data class WalkResult(val path: List<PosDir>, val termination: WalkTermination)

fun doWalk(initialState: PosDir, map: List<String>): WalkResult {
    var currentState = initialState
    val visitedStates = mutableSetOf<PosDir>()
    fun inBounds(xy: XY): Boolean = xy.y in map.indices && xy.x in map[0].indices
    while (inBounds(currentState.pos)) {
        if (!visitedStates.add(currentState)) {
            return WalkResult(visitedStates.toList(), WalkTermination.Loop)
        }
        val newState = currentState.step()
        if (!inBounds(newState.pos)) {
            break
        }
        currentState = if (map[newState.pos.y][newState.pos.x] == '#') {
            currentState.turnRight()
        } else {
            newState
        }
    }
    return WalkResult(visitedStates.toList(), WalkTermination.OffEdge)
}

object Day06Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val map = readInputLines("inputs/day06.txt")
        val walkResult = doWalk(PosDir(findStartingPos(map), XY(0, -1)), map)
        println(walkResult.path.map { it.pos }.distinct().size)
    }
}

fun setObstruction(map: List<String>, pos: XY): List<String> {
    return map.mapIndexed { index, s ->
        if (index == pos.y) {
            s.replaceRange(pos.x .. pos.x, "#")
        } else {
            s
        }
    }
}

object Day06Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val map = readInputLines("inputs/day06.txt")
        val initialState = PosDir(findStartingPos(map), XY(0, -1))
        val pristineWalkResult = doWalk(initialState, map)
        val loops = pristineWalkResult.path.map { it.pos }.distinct().count { candidate ->
            val experimentWalkResult = doWalk(initialState, setObstruction(map, candidate))
            experimentWalkResult.termination == WalkTermination.Loop
        }
        println(loops)
    }
}