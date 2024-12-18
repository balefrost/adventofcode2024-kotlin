package org.balefrost.aoc2024.day17

import kotlinx.collections.immutable.*
import org.balefrost.aoc2024.readInputLines

data class State(
    val registers: Map<String, Long>,
    val program: List<Byte>,
    val ip: Int,
    val output: PersistentList<Byte>
) {
    fun step(): State? {
        if (ip >= program.size) {
            return null
        }

        val newState = when (instruction) {
            0 -> setA(a shr combo.toInt())
            1 -> setB(b xor literal)
            2 -> setB(combo % 8)
            3 -> if (a == 0L) this else setIP(literal.toInt() - 2)
            4 -> setB(b xor c)
            5 -> out((combo % 8).toByte())
            6 -> setB(a shr combo.toInt())
            7 -> setC(a shr combo.toInt())
            else -> error("invalid opcode")
        }.next()
        return newState
    }

    val a: Long get() = registers.getValue("A")
    val b: Long get() = registers.getValue("B")
    val c: Long get() = registers.getValue("C")
    val instruction: Int get() = if (ip < program.size) program[ip].toInt() else -1

    fun setA(a: Long) = copy(registers = registers + ("A" to a))
    private fun setB(b: Long) = copy(registers = registers + ("B" to b))
    private fun setC(c: Long) = copy(registers = registers + ("C" to c))
    private fun setIP(ip: Int) = copy(ip = ip)
    private fun out(value: Byte) = copy(output = output + value)

    private fun next() = copy(ip = ip + 2)

    val literal: Long get() = if (ip < program.size) program[ip + 1].toLong() else -1

    val combo: Long
        get() {
            return when (val oper = literal.toInt()) {
                -1 -> -1
                0, 1, 2, 3 -> oper.toLong()
                4 -> registers.getValue("A")
                5 -> registers.getValue("B")
                6 -> registers.getValue("C")
                else -> error("invalid combo operand")
            }
        }
}

fun parseInput(lines: List<String>): State {
    val registerRegex = """Register (\w+): (\d+)""".toRegex()
    val programRegex = """Program: (.*)""".toRegex()
    val registers = mutableMapOf<String, Long>()

    val linesIter = lines.iterator()
    while (linesIter.hasNext()) {
        val line = linesIter.next()
        if (line.isBlank()) {
            break
        }
        val (_, name, value) = registerRegex.matchEntire(line)!!.groupValues
        registers[name] = value.toLong()
    }

    val instructions = mutableListOf<Byte>()

    while (linesIter.hasNext()) {
        val line = linesIter.next()
        val (_, program) = programRegex.matchEntire(line)!!.groupValues
        instructions += program.split(",").map { it.toByte() }
    }

    return State(registers, instructions, 0, persistentListOf())
}

fun runProgram(initialState: State): Sequence<State> {
    return generateSequence(initialState, State::step)
}

fun outputSequence(initialState: State): Sequence<Byte> {
    return sequence {
        var oldOutputLength = initialState.output.size
        for (newState in runProgram(initialState)) {
            if (newState.output.size > oldOutputLength) {
                yield(newState.output.last())
                oldOutputLength += 1
            }
        }
    }
}

object Day17Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day17.txt")
        val initialState = parseInput(lines)

        println(outputSequence(initialState).joinToString(","))
    }
}

object Day17Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        // 2,4    top:    b <- a % 8          // b in [0, 7]
        // 1,5            b <- b xor 5        // b in [0, 7]
        // 7,5            c <- a >> b
        // 4,3            b <- b xor c
        // 1,6            b <- b xor 6
        // 0,3            a <- a >> 3
        // 5,5            out <- b % 8
        // 3,0            if a != 0  goto top


        // do {
        //   b = (a % 8) xor 5                // b in [0, 7]
        //   b = ((a >> b) xor b) xor 6
        //   a = a >> 3
        //   print(b % 8)
        // } while (a != 0)


        val lines = readInputLines("inputs/day17.txt")

        val initialState = parseInput(lines)

        val options = (0L..<1024L).map { candidate ->
            outputSequence(initialState.setA(candidate)).first() to candidate
        }.groupBy({ it.first }, { it.second }).mapValues { (_, v) -> v.toSet() }

        var allCandidateNumbers = options.getValue(initialState.program.first()).toSet()
        for ((index, byte) in initialState.program.withIndex().drop(1)) {
            allCandidateNumbers = mutableSetOf<Long>().also { newNumbers ->
                for (candidateNumber in allCandidateNumbers) {
                    val candidateShiftedRight = candidateNumber shr (3 * index)
                    val possibleNextValues = options.getValue(byte)
                    for (next in possibleNextValues) {
                        if (next and 0x7fL == candidateShiftedRight and 0x7fL) {
                            newNumbers.add(candidateNumber or ((next and 0x380) shl (3 * index)))
                        }
                    }
                }
            }
        }
        println(allCandidateNumbers.filter { candidate ->
            outputSequence(initialState.setA(candidate)).toList() == initialState.program
        }.min())

    }
}
