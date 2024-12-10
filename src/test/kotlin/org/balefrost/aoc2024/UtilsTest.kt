package org.balefrost.aoc2024

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class UtilsTest {
    @Test
    fun `test sortPartiallyOrdered when all deps are present in the list to be ordered`() {
        val deps = mapOf(
            5 to setOf(4),
            4 to setOf(3),
            3 to setOf(2),
            2 to setOf(1)
        )
        assertThat(
            sortPartiallyOrdered(
                listOf(5, 4, 3, 2, 1)
            ) { deps.getOrDefault(it, emptySet()) }.toList(),
            equalTo(listOf(1, 2, 3, 4, 5))
        )
    }

    @Test
    fun `test sortPartiallyOrdered when some deps are discovered during iteration`() {
        val deps = mapOf(
            5 to setOf(4),
            4 to setOf(3),
            3 to setOf(2),
            2 to setOf(1)
        )
        assertThat(
            sortPartiallyOrdered(
                listOf(5, 1)
            ) { deps.getOrDefault(it, emptySet()) }.toList(),
            equalTo(listOf(1, 2, 3, 4, 5))
        )
    }

    @Test
    fun `test sortPartiallyOrdered with multiple dependencies`() {
        val deps = mapOf(
            5 to setOf(4, 3),
            4 to setOf(2),
            3 to setOf(2),
            2 to setOf(1)
        )
        assertThat(
            sortPartiallyOrdered(
                listOf(5, 4, 3, 2, 1)
            ) { deps.getOrDefault(it, emptySet()) }.toList(),
            anyOf(
                equalTo(listOf(1, 2, 3, 4, 5)),
                equalTo(listOf(1, 2, 4, 3, 5))
            )
        )
    }

    @Test
    fun `test sortPartiallyOrdered with cycle`() {
        val deps = mapOf(
            4 to setOf(2, 20),
            3 to setOf(4, 40),
            2 to setOf(3, 30),
            1 to setOf(2, 20)
        )
        val ex = assertThrows<IllegalArgumentException> {
            sortPartiallyOrdered(
                listOf(1)
            ) { deps.getOrDefault(it, emptySet()) }.toList()
        }
        assertThat(ex.message, containsString("2 -> 3 -> 4 -> 2"))
    }

    @Test
    fun `test sortPartiallyOrdered with false cycle`() {
        val deps = mapOf(
            4 to setOf(5),
            3 to setOf(4, 5),
            2 to setOf(3, 5),
            1 to setOf(2, 5)
        )
        assertThat(
            sortPartiallyOrdered(listOf(1)) { deps.getOrDefault(it, emptySet()) }.toList(),
            equalTo(listOf(5, 4, 3, 2, 1))
        )
    }

    @Test
    fun `binarySearch matches first item`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(0, it) },
            equalTo(0)
        )
    }

    @Test
    fun `binarySearch matches last item`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(10, it) },
            equalTo(10)
        )
    }

    @Test
    fun `binarySearch matches exact middle item`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(5, it) },
            equalTo(5)
        )
    }

    @Test
    fun `binarySearch matches item in first half`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(2, it) },
            equalTo(2)
        )
    }

    @Test
    fun `binarySearch matches item in second half`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(7, it) },
            equalTo(7)
        )
    }

    @Test
    fun `binarySearch finds insertion point before start`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(-1, it) },
            equalTo(0.inv())
        )
    }

    @Test
    fun `binarySearch finds insertion point after end`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(11, it) },
            equalTo(11.inv())
        )
    }

    @Test
    fun `binarySearch finds insertion point between items`() {
        assertThat(
            binarySearch((0..10).map { it.toDouble() }.toList()) { compareValues(3.5, it) },
            equalTo(4.inv())
        )
    }

    @Test
    fun `cartesian product of zero options`() {
        assertThat(
            cartesianProduct<Int>(emptyList()).toList(),
            equalTo(listOf(emptyList<Int>()))
        )
    }

    @Test
    fun `cartesian product of one set of options`() {
        assertThat(
            cartesianProduct(listOf(listOf("a", "b", "c"))).toList(),
            equalTo(
                listOf(
                    listOf("a"),
                    listOf("b"),
                    listOf("c"),
                )
            )
        )
    }

    @Test
    fun `cartesian product of many singleton sets of options`() {
        assertThat(
            cartesianProduct(listOf(listOf("a"), listOf("b"), listOf("c"))).toList(),
            equalTo(listOf(listOf("a", "b", "c")))
        )
    }

    @Test
    fun `cartesian product of three sets of two options each`() {
        assertThat(
            cartesianProduct(
                listOf(
                    listOf("a", "b"),
                    listOf("j", "k"),
                    listOf("x", "y")
                )
            ).toList(),
            equalTo(
                listOf(
                    listOf("a", "j", "x"),
                    listOf("a", "j", "y"),
                    listOf("a", "k", "x"),
                    listOf("a", "k", "y"),
                    listOf("b", "j", "x"),
                    listOf("b", "j", "y"),
                    listOf("b", "k", "x"),
                    listOf("b", "k", "y"),
                )
            )
        )
    }

    @Test
    fun `XY turns right`() {
        assertThat(XY(1, 0).turnRight(), equalTo(XY(0, 1)))
        assertThat(XY(0, 1).turnRight(), equalTo(XY(-1, 0)))
        assertThat(XY(-1, 0).turnRight(), equalTo(XY(0, -1)))
        assertThat(XY(0, -1).turnRight(), equalTo(XY(1, 0)))
    }

    @Test
    fun `StringBased2dMap contains checks bounds`() {
        val map = StringBased2DMap("""
            0
            01
            012
            0123
            01234
        """.trimIndent().lines())

        assertThat(XY(0, 0) in map, equalTo(true))
        assertThat(XY(0, 1) in map, equalTo(true))
        assertThat(XY(0, 2) in map, equalTo(true))
        assertThat(XY(0, 3) in map, equalTo(true))
        assertThat(XY(0, 4) in map, equalTo(true))
        assertThat(XY(1, 1) in map, equalTo(true))
        assertThat(XY(2, 2) in map, equalTo(true))
        assertThat(XY(3, 3) in map, equalTo(true))
        assertThat(XY(4, 4) in map, equalTo(true))

        assertThat(XY(-1, 0) in map, equalTo(false))
        assertThat(XY(-1, 1) in map, equalTo(false))
        assertThat(XY(-1, 2) in map, equalTo(false))
        assertThat(XY(-1, 3) in map, equalTo(false))
        assertThat(XY(-1, 4) in map, equalTo(false))
        assertThat(XY(0, -1) in map, equalTo(false))
        assertThat(XY(0, 5) in map, equalTo(false))
        assertThat(XY(1, 0) in map, equalTo(false))
        assertThat(XY(1, 5) in map, equalTo(false))
        assertThat(XY(2, 1) in map, equalTo(false))
        assertThat(XY(2, 5) in map, equalTo(false))
        assertThat(XY(3, 2) in map, equalTo(false))
        assertThat(XY(3, 5) in map, equalTo(false))
        assertThat(XY(4, 3) in map, equalTo(false))
        assertThat(XY(4, 5) in map, equalTo(false))
        assertThat(XY(5, 4) in map, equalTo(false))
    }

    @Test
    fun `StringBased2dMap get within bounds`() {
        val map = StringBased2DMap("""
            0123
            4567
        """.trimIndent().lines())

        assertThat(map[XY(0, 0)], equalTo('0'))
        assertThat(map[XY(1, 0)], equalTo('1'))
        assertThat(map[XY(2, 0)], equalTo('2'))
        assertThat(map[XY(3, 0)], equalTo('3'))
        assertThat(map[XY(0, 1)], equalTo('4'))
        assertThat(map[XY(1, 1)], equalTo('5'))
        assertThat(map[XY(2, 1)], equalTo('6'))
        assertThat(map[XY(3, 1)], equalTo('7'))
    }

    @Test
    fun `StringBased2dMap out of bounds`() {
        val map = StringBased2DMap("""
            0123
            4567
        """.trimIndent().lines())

        assertThrows<IndexOutOfBoundsException> {
            map[XY(-10, 0)]
        }

        assertThrows<IndexOutOfBoundsException> {
            map[XY(0, -10)]
        }
    }
}