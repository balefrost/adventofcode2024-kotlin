package org.balefrost.aoc2024.day24

import org.balefrost.aoc2024.readInputLines
import java.io.File
import kotlin.reflect.KClass

sealed interface Gate {
    fun changeOutput(newOutput: String): Gate

    val symbol: String
    val output: String
    val inputs: Set<String>
}

data class Constant(val value: Boolean, override val output: String) : Gate {
    override fun changeOutput(newOutput: String): Constant = copy(output = newOutput)

    override val symbol: String
        get() = "CONST"
    override val inputs: Set<String> get() = emptySet()
}

data class And(val a: String, val b: String, override val output: String) : Gate {
    override fun changeOutput(newOutput: String): And = copy(output = newOutput)
    override val symbol: String
        get() = "AND"
    override val inputs: Set<String> = setOf(a, b)
}

data class Or(val a: String, val b: String, override val output: String) : Gate {
    override fun changeOutput(newOutput: String): Or = copy(output = newOutput)
    override val symbol: String
        get() = "OR"
    override val inputs: Set<String> = setOf(a, b)
}

data class Xor(val a: String, val b: String, override val output: String) : Gate {
    override fun changeOutput(newOutput: String): Xor = copy(output = newOutput)
    override val symbol: String
        get() = "XOR"
    override val inputs: Set<String> = setOf(a, b)
}

class GatesLookup(val gates: List<Gate>) {
    val byOutput = gates.associateBy { it.output }
    val byInput = gates.flatMap { gate ->
        gate.inputs.map { input ->
            input to gate
        }
    }.groupBy({ it.first }, { it.second })
    val byInputSet = gates.map { gate ->
        gate.inputs.toSet() to gate
    }.groupBy({ it.first }, { it.second })

    val wires: Sequence<String> = sequence {
        for (gate in gates) {
            yieldAll(gate.inputs)
            yield(gate.output)
        }
    }.distinct()

    private val gateToIndex = gates.withIndex().associate { (index, gate) -> gate to index }

    fun getId(gate: Gate): Int = gateToIndex.getValue(gate)

    fun find(
        type: KClass<out Gate>? = null,
        inputs: Set<String> = emptySet(),
        output: String? = null
    ): Set<Gate> {
        val result = gates.toMutableSet()
        if (type != null) {
            result.removeIf { !type.isInstance(it) }
        }
        if (output != null) {
            result.removeIf { it.output != output }
        }
        result.removeIf { gate -> inputs.any { it !in gate.inputs } }

        return result
    }
}

fun parseInput(lines: Iterable<String>): GatesLookup {
    val gates = mutableListOf<Gate>()
    val iter = lines.iterator()
    for (line in iter) {
        if (line.isBlank()) {
            break
        }
        val (key, value) = line.split(": ".toRegex())
        val boolValue = when (value) {
            "0" -> false
            "1" -> true
            else -> error("invalid wire value '$value'")
        }
        gates.add(Constant(boolValue, key))
    }

    val gateRegex = """(\w+) (\w+) (\w+) -> (\w+)""".toRegex()
    for (line in iter) {
        val (a, gate, b, dest) = checkNotNull(gateRegex.matchEntire(line)) { "invalid line '$line'" }.groupValues.drop(1)
        gates.add(
            when (gate) {
                "AND" -> And(a, b, dest)
                "OR" -> Or(a, b, dest)
                "XOR" -> Xor(a, b, dest)
                else -> error("invalid gate '$gate'")
            }
        )
    }

    return GatesLookup(gates)
}

fun computeValues(gates: GatesLookup): Map<String, Boolean>? {
    val result = mutableMapOf<String, Boolean?>()
    val stack = mutableSetOf<String>()
    fun helper(name: String): Boolean? {
        val existing = result[name]
        if (existing != null) {
            return existing
        }

        if (!stack.add(name)) {
            // cycle detected
            result[name] = null
            return null
        }
        val definition = gates.byOutput.getValue(name)
        val inputValues = definition.inputs.map(::helper)
        if (inputValues.any { it == null }) {
            result[name] = null
            return null
        }
        val r = when (definition) {
            is Constant -> definition.value
            is And -> inputValues.filterNotNull().reduce(Boolean::and)
            is Or -> inputValues.filterNotNull().reduce(Boolean::or)
            is Xor -> inputValues.filterNotNull().reduce(Boolean::xor)
        }

        stack.remove(name)
        result[name] = r
        return r
    }
    for (name in gates.byOutput.keys) {
        helper(name)
    }
    return result.filterValues { v -> v != null } as Map<String, Boolean>
}

fun makeNumber(bitsHighToLow: Iterable<Boolean>): Long {
    return bitsHighToLow.fold(0L) { acc, bit ->
        (acc shl 1) or if (bit) 1 else 0
    }
}

fun findNames(results: Map<String, Boolean>, prefix: String): List<String> {
    return results.keys.filter { it.startsWith(prefix) }.sorted()
}

fun makeNumber(results: Map<String, Boolean>, prefix: String): Long {
    val bits = findNames(results, prefix).sortedDescending().map(results::getValue)
    return makeNumber(bits)
}


object Day24Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day24.txt")
//        val lines = """
//            x00: 1
//            x01: 1
//            x02: 1
//            y00: 0
//            y01: 1
//            y02: 0
//
//            x00 AND y00 -> z00
//            x01 XOR y01 -> z01
//            x02 OR y02 -> z02
//        """.trimIndent().lines()
//        val lines = """
//            x00: 1
//            x01: 0
//            x02: 1
//            x03: 1
//            x04: 0
//            y00: 1
//            y01: 1
//            y02: 1
//            y03: 1
//            y04: 1
//
//            ntg XOR fgs -> mjb
//            y02 OR x01 -> tnw
//            kwq OR kpj -> z05
//            x00 OR x03 -> fst
//            tgd XOR rvg -> z01
//            vdt OR tnw -> bfw
//            bfw AND frj -> z10
//            ffh OR nrd -> bqk
//            y00 AND y03 -> djm
//            y03 OR y00 -> psh
//            bqk OR frj -> z08
//            tnw OR fst -> frj
//            gnj AND tgd -> z11
//            bfw XOR mjb -> z00
//            x03 OR x00 -> vdt
//            gnj AND wpb -> z02
//            x04 AND y00 -> kjc
//            djm OR pbm -> qhw
//            nrd AND vdt -> hwm
//            kjc AND fst -> rvg
//            y04 OR y02 -> fgs
//            y01 AND x02 -> pbm
//            ntg OR kjc -> kwq
//            psh XOR fgs -> tgd
//            qhw XOR tgd -> z09
//            pbm OR djm -> kpj
//            x03 XOR y03 -> ffh
//            x00 XOR y04 -> ntg
//            bfw OR bqk -> z06
//            nrd XOR fgs -> wpb
//            frj XOR qhw -> z04
//            bqk OR frj -> z07
//            y03 OR x01 -> nrd
//            hwm AND bqk -> z03
//            tgd XOR rvg -> z12
//            tnw OR pbm -> gnj
//        """.trimIndent().lines()

        val gates = parseInput(lines)
        val results = computeValues(gates)!!
        println(makeNumber(results, "z"))
    }
}

fun upstreamNames(name: String, gates: Map<String, Gate>): Set<String> {
    val result = mutableSetOf<String>()
    val remaining = ArrayDeque<String>()
    remaining.add(name)
    while (remaining.isNotEmpty()) {
        val item = remaining.removeFirst()
        if (result.add(item)) {
            remaining.addAll(gates.getValue(item).inputs)
        }
    }
    return result
}

inline fun <reified T> findGate(
    gates: GatesLookup,
    a: String?,
    b: String?
): List<String> {
    return gates.byOutput.entries.filter { (_, gate) -> gate is T && gate.inputs.toSet() == setOf(a, b) }.map { it.key }
}

object Day24Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day24.txt")
        val gates = parseInput(lines)

        val originalResults = computeValues(gates)!!
        val xValue = makeNumber(originalResults, "x")
        val yValue = makeNumber(originalResults, "y")
        val expectedZValue = xValue + yValue
        val xNames = findNames(originalResults, "x")
        val zNames = findNames(originalResults, "z")

        data class HalfAdder(val xorGate: Xor, val andGate: And) {
            val inputs: Set<String> get() = xorGate.inputs
        }

        val halfAdders = gates.gates.filterIsInstance<Xor>().map { xorGate ->
            val andGate = gates.find(type = And::class, inputs = xorGate.inputs).single() as And
            HalfAdder(xorGate, andGate)
        }.toSet()
        val secondHalfAdders = halfAdders.filter { it.inputs.none { it.startsWith("x") } }.toSet()
        val secondHalfAdderInputs = secondHalfAdders.flatMap { it.inputs }
        val firstHalfAdders = halfAdders.filterNot { it in secondHalfAdders }
        val zeroBitHalfAdder = firstHalfAdders.single { it.xorGate.inputs.contains(xNames.first()) }
        val carryCombinerGates = gates.gates.filterIsInstance<Or>()

        val carryCombinerInputs = carryCombinerGates.flatMap { it.inputs }.toSet()

        val gatesWithWrongOutput = mutableSetOf<Gate>()
        for (halfAdder in firstHalfAdders) {
            if (halfAdder == zeroBitHalfAdder) {
                if (halfAdder.andGate.output !in secondHalfAdderInputs) {
                    gatesWithWrongOutput.add(halfAdder.andGate)
                }
                continue
            }
            if (halfAdder.xorGate.output !in secondHalfAdderInputs) {
                gatesWithWrongOutput.add(halfAdder.xorGate)
            }
            if (halfAdder.andGate.output !in carryCombinerInputs) {
                gatesWithWrongOutput.add(halfAdder.andGate)
            }
        }

        for (halfAdder in secondHalfAdders) {
            if (halfAdder.xorGate.output !in zNames) {
                gatesWithWrongOutput.add(halfAdder.xorGate)
            }
            if (halfAdder.andGate.output !in carryCombinerInputs) {
                gatesWithWrongOutput.add(halfAdder.andGate)
            }
        }

        for (orGate in carryCombinerGates) {
            if (orGate.output !in secondHalfAdderInputs && orGate.output != zNames.last()) {
                gatesWithWrongOutput.add(orGate)
            }
        }

        check(gatesWithWrongOutput.size == 8)

        val matching =
            gatesWithWrongOutput.map { it.output }.swaps
                .filter { swaps ->
                    val replacements = swaps.flatMap { (a, b) -> listOf(a to b, b to a) }.toMap()
                    val newGates = GatesLookup(gates.gates.map { gate ->
                        val replacement = replacements[gate.output]
                        if (replacement != null) {
                            gate.changeOutput(replacement)
                        } else {
                            gate
                        }
                    })
                    val computed = computeValues(newGates)
                    if (computed == null) {
                        return@filter false
                    }
                    makeNumber(computed, "z") == expectedZValue
                }.toList()

        println("matching")
        matching.forEach(::println)

        if (matching.size == 1) {
            println(matching.first().flatMap { (a, b) -> listOf(a, b) }.sorted().joinToString(","))
        }
    }
}

object WriteDot {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day24.txt")
        val gates = parseInput(lines)
        File("out.dot").writeText(makeDot(gates))
    }
}

fun makeDot(gates: GatesLookup): String {
    val xNames = gates.byOutput.keys.filter { it.startsWith("x") }
    val yNames = gates.byOutput.keys.filter { it.startsWith("y") }
    val zNames = gates.byOutput.keys.filter { it.startsWith("z") }
    return buildString {
        appendLine("digraph {")
        for ((name, gate) in gates.byOutput) {
            when (gate) {
                is And -> {
//                    appendLine("$name [label=\"AND: $name\"]")
                    appendLine("$name [label=\"AND\"]")
                }

                is Or -> {
//                    appendLine("$name [label=\"OR: $name\"]")
                    appendLine("$name [label=\"OR\"]")
                }

                is Xor -> {
//                    appendLine("$name [label=\"XOR: $name\"]")
                    appendLine("$name [label=\"XOR\"]")
                }

                is Constant -> {
//                    appendLine("$name [label=\"$name (${if (gate.value) "1" else "0"})\"]")
                    appendLine("$name [label=\"$name\"]")
                }
            }
        }
        appendLine("synthetic_start [style=invisible, ordering=out]")
        val xIter = xNames.iterator()
        val yIter = yNames.iterator()
        while (xIter.hasNext() || yIter.hasNext()) {
            if (xIter.hasNext()) {
                appendLine("synthetic_start -> ${xIter.next()} [style=invisible]")
            }
            if (yIter.hasNext()) {
                appendLine("synthetic_start -> ${yIter.next()} [style=invisible]")
            }
        }
        for ((name, gate) in gates.byOutput) {
            if (name.startsWith("z")) {
                appendLine("${name}_out [label=\"$name\"]")
            }
        }
        for (gate in gates.gates.sortedBy { it.inputs.joinToString("__") }) {
            for ((dep, label) in gate.inputs.asSequence().zip(sequenceOf("a", "b", "c", "d"))) {
//                appendLine("$dep -> $name [label=\"$label\"]")
                appendLine("$dep -> ${gate.output} [label=\"$dep\"]")
            }
        }
        for ((name, gate) in gates.byOutput) {
            if (name.startsWith("z")) {
                appendLine("$name -> ${name}_out")
            }
        }
        run {
            appendLine("subgraph inputs {")
            appendLine("rank=\"source\"")
            for (s in xNames) {
                appendLine(s)
            }
            for (s in yNames) {
                appendLine(s)
            }
            appendLine("}")
        }
//        run {
//            appendLine("subgraph internal {")
//            for (s in gates.keys) {
//                if (s !in xNames && s !in yNames && s !in zNames) {
//                    appendLine(s)
//                }
//            }
//            appendLine("}")
//        }
        run {
            appendLine("subgraph outputs {")
            appendLine("rank=\"sink\"")
            for (s in zNames) {
                appendLine("${s}_out")
            }
            appendLine("}")
        }
        appendLine("}")
    }
}

val <T> List<T>.swaps: Sequence<Set<Pair<T, T>>>
    get() = sequence {
        check(this@swaps.size % 2 == 0) { "needs an even number of items" }
        if (this@swaps.isEmpty()) {
            yield(emptySet())
            return@sequence
        }
        val first = this@swaps[0]
        for (i in 1..<this@swaps.size) {
            val second = this@swaps[i]
            val tail = this@swaps.subList(1, i) + this@swaps.subList(i + 1, this@swaps.size)
            for (tailSwap in (tail).swaps) {
                yield(setOf(first to second) + tailSwap)
            }
        }
    }

