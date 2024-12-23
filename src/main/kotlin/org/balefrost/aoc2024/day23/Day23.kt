package org.balefrost.aoc2024.day23

import org.balefrost.aoc2024.readInputLines

fun findTriples(neighbors: Map<String, Set<String>>): Set<Set<String>> {
    val triples = mutableSetOf<Set<String>>()
    for ((a, aNeighbors) in neighbors.filterKeys { it.startsWith("t") }) {
        for (b in aNeighbors) {
            val bNeighbors = neighbors.getValue(b)
            for (c in aNeighbors.intersect(bNeighbors)) {
                triples.add(sortedSetOf(a, b, c))
            }
        }
    }
    return triples
}

object Day23Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day23.txt")

        val lineRegex = """(\w{2})-(\w{2})""".toRegex()
        val neighbors = lines.flatMap {
            val (a, b) = lineRegex.matchEntire(it)!!.groupValues.drop(1)
            listOf(a to b, b to a)
        }.groupBy({ it.first}, {it.second}).mapValues { (_, v) -> v.toSet() }

        println(findTriples(neighbors).size)
    }
}

object Day23Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day23.txt")

        val lineRegex = """(\w{2})-(\w{2})""".toRegex()
        val neighbors = lines.flatMap {
            val (a, b) = lineRegex.matchEntire(it)!!.groupValues.drop(1)
            listOf(a to b, b to a)
        }.groupBy({ it.first}, {it.second}).mapValues { (_, v) -> v.toSet() }

        fun findCliques(neighbors: Map<String, Set<String>>): Sequence<Set<String>> {
            return sequence {
                val workSet = mutableSetOf<String>()
                suspend fun SequenceScope<Set<String>>.helper(toTest: List<String>) {
                    if (toTest.isEmpty()) {
                        yield(workSet.toSet())
                        return
                    }
                    val item = toTest.first()
                    val rest = toTest.subList(1, toTest.size)
                    if (workSet.all { item in neighbors.getValue(it) }) {
                        workSet.add(item)
                        helper(rest)
                        workSet.remove(item)
                    }

                    helper(rest)
                }
                for ((item, adjacent) in neighbors) {
                    workSet.clear()
                    workSet.add(item)
                    helper(adjacent.toList())
                }
            }
        }

        findCliques(neighbors).maxBy { it.size }.sorted().joinToString(",").also(::println)
    }
}
