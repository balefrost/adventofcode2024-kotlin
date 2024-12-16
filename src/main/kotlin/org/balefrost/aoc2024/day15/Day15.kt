package org.balefrost.aoc2024.day15

import org.balefrost.aoc2024.*

data class Input(val map: Map2D, val dirsLines: List<String>) {
    val allMoves: String = dirsLines.joinToString("")
}

fun Map2D.positionsToClearOrWall(pos: XY, dir: XY) = sequence {
    var pp = pos
    yield(pp)
    while (this@positionsToClearOrWall[pp] != '#' && this@positionsToClearOrWall[pp] != '.') {
        pp += dir
        yield(pp)
    }
}


fun parseInput(lines: Iterable<String>): Input {
    val iter = lines.iterator()
    val mapLines = mutableListOf<String>()
    val dirsLines = mutableListOf<String>()
    while (iter.hasNext()) {
        val line = iter.next()
        if (line.isBlank()) {
            break
        }
        mapLines += line
    }
    while (iter.hasNext()) {
        dirsLines += iter.next()
    }
    return Input(makeMutableMapFromLines(mapLines, '#'), dirsLines)
}

object Day15Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day15.txt")
//        val lines = """
//            ##########
//            #..O..O.O#
//            #......O.#
//            #.OO..O.O#
//            #..O@..O.#
//            #O#..O...#
//            #O..O..O.#
//            #.OO.O.OO#
//            #....O...#
//            ##########
//
//            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
//            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
//            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
//            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
//            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
//            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
//            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
//            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
//            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
//            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
//        """.trimIndent().lines()
        val input = parseInput(lines)
        var robotPos = input.map.positions.first { input.map[it] == '@' }
        val currentMap = input.map.toMutableMap2D()
        for (move in input.allMoves) {
            val dir = when (move) {
                '^' -> XY(0, -1)
                '>' -> XY(1, 0)
                'v' -> XY(0, 1)
                '<' -> XY(-1, 0)
                else -> error("Invalid dir $move")
            }

            val inLine = currentMap.positionsToClearOrWall(robotPos, dir).map { it to currentMap[it] }.toList()
            if (inLine.last().second == '#') {
                continue
            }

            for ((into, from) in inLine.asReversed().zipWithNext()) {
                check(currentMap[into.first] == '.')
                currentMap[into.first] = currentMap[from.first]
                currentMap[from.first] = '.'
            }

            robotPos += dir
        }
        val gpsSum = currentMap.positions.sumOf { pos ->
            when (currentMap[pos]) {
                'O' -> 100 * pos.y + pos.x
                else -> 0
            }
        }

        println(gpsSum)
    }
}

object Day15Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day15.txt")
//        val lines = """
//            #######
//            #...#.#
//            #.....#
//            #..OO@#
//            #..O..#
//            #.....#
//            #######
//
//            <vv<<^^<<^^
//        """.trimIndent().lines()
//        val lines = """
//            ##########
//            #..O..O.O#
//            #......O.#
//            #.OO..O.O#
//            #..O@..O.#
//            #O#..O...#
//            #O..O..O.#
//            #.OO.O.OO#
//            #....O...#
//            ##########
//
//            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
//            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
//            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
//            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
//            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
//            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
//            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
//            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
//            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
//            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
//        """.trimIndent().lines()
        val input = run {
            val inp = parseInput(lines)
            val expandedLines = (0..<inp.map.dims.h).map { y ->
                val line = (0..<inp.map.dims.w).map { x -> inp.map[XY(x, y)] }
                line.joinToString("") {
                    when (it) {
                        '#' -> "##"
                        'O' -> "[]"
                        '.' -> ".."
                        '@' -> "@."
                        else -> error("invalid data $it")
                    }
                }
            }
            Input(makeMutableMapFromLines(expandedLines, '#'), inp.dirsLines)
        }

        var robotPos = input.map.positions.first { input.map[it] == '@' }
        val currentMap = input.map.toMutableMap2D()

        fun members(pos: XY): List<XY> {
            return when(currentMap[pos]) {
                '[' -> listOf(pos, pos + XY(1, 0))
                ']' -> listOf(pos, pos + XY(-1, 0))
                else -> listOf(pos)
            }
        }

        fun tryPush(pos: XY, dir: XY): Boolean {
            val allPositions = mutableSetOf<XY>()
            val fringe = ArrayDeque(members(pos))
            while (fringe.isNotEmpty()) {
                val p = fringe.removeFirst()
                if (currentMap[p] !in setOf('[', ']', '@')) {
                    continue
                }
                allPositions += p
                fringe.addAll(members(p + dir).filterNot { it in allPositions })
            }

            val canMove = allPositions.all { currentMap[it + dir] == '.' || (it + dir) in allPositions }

            if (canMove) {
                // TODO: order updates to avoid the need for a copy
                val originalMap = currentMap.toMutableMap2D()
                allPositions.forEach { currentMap[it] = '.' }
                allPositions.forEach { currentMap[it + dir] = originalMap[it] }

                return true
            }

            return false
        }

        for (move in input.allMoves) {
            val dir = when (move) {
                '^' -> XY(0, -1)
                '>' -> XY(1, 0)
                'v' -> XY(0, 1)
                '<' -> XY(-1, 0)
                else -> error("Invalid dir $move")
            }

            if (tryPush(robotPos, dir)) {
                robotPos += dir
            }
        }
        val gpsSum = currentMap.positions.sumOf { pos ->
            when (currentMap[pos]) {
                '[' -> 100 * pos.y + pos.x
                else -> 0
            }
        }

        println(gpsSum)
    }
}
