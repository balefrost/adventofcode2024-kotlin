package org.balefrost.aoc2024.day13

import org.balefrost.aoc2024.LongXY
import org.balefrost.aoc2024.readInputLines
import kotlin.math.abs

data class Machine(val a: LongXY, val b: LongXY, val p: LongXY)

fun gcd(a: Long, b: Long): Long {
    var x = abs(a)
    var y = abs(b)
    while (y != 0L) {
        val temp = x % y
        x = y
        y = temp
    }
    return x
}

fun lcm(a: Long, b: Long): Long {
    val g = gcd(a, b)
    return a / g * b
}

data class LongRatio(val n: Long, val d: Long) {
    constructor(n: Long) : this(n, 1L)

    fun reduce(): LongRatio {
        val g = gcd(n, d)
        var n = n
        var d = d
        if (d < 0) {
            n = -n
            d = -d
        }
        n /= g
        d /= g

        if (n == this.n && d == this.d) {
            return this
        }
        return LongRatio(n, d)
    }

    operator fun compareTo(other: LongRatio): Int {
        val denomLcm = lcm(d, other.d)
        val thisMult = denomLcm / d
        val otherMult = denomLcm / other.d
        return compareValues(n * thisMult, other.n * otherMult)
    }

    operator fun div(factor: LongRatio): LongRatio {
        return LongRatio(n * factor.d, d * factor.n)
    }

    override fun toString(): String {
        return "$n / $d"
    }

    operator fun times(factor: Long): LongRatio {
        return LongRatio(n * factor, d)
    }

    operator fun plus(other: LongRatio): LongRatio {
        val denomLcm = lcm(d, other.d)
        val thisMult = denomLcm / d
        val otherMult = denomLcm / other.d
        return LongRatio(n * thisMult + other.n * otherMult, denomLcm)
    }

    operator fun times(factor: LongRatio): LongRatio {
        return LongRatio(n * factor.n, d * factor.d)
    }

    operator fun unaryMinus(): LongRatio {
        return LongRatio(-n, d)
    }
}

fun parseMachines(lines: Iterable<String>): List<Machine> {
    val machines = mutableListOf<Machine>()
    val iter = lines.iterator()
    while (iter.hasNext()) {
        val a = checkNotNull("""Button A: X\+(\d+), Y\+(\d+)""".toRegex().matchEntire(iter.next()))
        val b = checkNotNull("""Button B: X\+(\d+), Y\+(\d+)""".toRegex().matchEntire(iter.next()))
        val c = checkNotNull("""Prize: X=(\d+), Y=(\d+)""".toRegex().matchEntire(iter.next()))
        val (ax, ay) = a.groupValues.drop(1).map(String::toLong)
        val (bx, by) = b.groupValues.drop(1).map(String::toLong)
        val (cx, cy) = c.groupValues.drop(1).map(String::toLong)
        if (iter.hasNext()) {
            check(iter.next().isBlank())
        }
        machines += Machine(LongXY(ax, ay), LongXY(bx, by), LongXY(cx, cy))
    }
    return machines
}

fun solve(machine: Machine): List<Long>? {
    // Gaussian elimination of two variables

    val equations = listOf(
        mutableListOf(LongRatio(machine.a.x), LongRatio(machine.b.x), LongRatio(-machine.p.x)),
        mutableListOf(LongRatio(machine.a.y), LongRatio(machine.b.y), LongRatio(-machine.p.y)),
    )

    fun divideRow(row: Int, factor: LongRatio) {
        for (i in equations[row].indices) {
            equations[row][i] = (equations[row][i] / factor).reduce()
        }
    }

    fun addMultOfRowToRow(srow: Int, drow: Int, factor: LongRatio) {
        for (i in equations[drow].indices) {
            equations[drow][i] = (equations[drow][i] + equations[srow][i] * factor).reduce()
        }
    }

    divideRow(0, equations[0][0])
    addMultOfRowToRow(0, 1, -equations[1][0])
    divideRow(1, equations[1][1])
    addMultOfRowToRow(1, 0, -equations[0][1])

    return if (equations[0][2].d == 1L && equations[1][2].d == 1L) {
        listOf(-equations[0][2].n, -equations[1][2].n)
    } else {
        null
    }
}

object Day13Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day13.txt")
        val machines = parseMachines(lines)

        val totalCost = machines.sumOf { machine ->
            val solution = solve(machine)
            if (solution != null) {
                val (a, b) = solution
                3 * a + b
            } else {
                0
            }
        }

        println(totalCost)
    }
}

object Day13Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day13.txt")
        val offset = 10000000000000L
        val machines = parseMachines(lines).map { (a, b, p) -> Machine(a, b, p + LongXY(offset, offset)) }

        val totalCost = machines.sumOf {
            val solution = solve(it)
            if (solution != null) {
                val (a, b) = solution
                3 * a + b
            } else {
                0
            }
        }

        println(totalCost)
    }
}
