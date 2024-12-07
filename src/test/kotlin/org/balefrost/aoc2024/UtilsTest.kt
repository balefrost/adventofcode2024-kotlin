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
            equalTo(0))
    }

    @Test
    fun `binarySearch matches last item`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(10, it) },
            equalTo(10))
    }

    @Test
    fun `binarySearch matches exact middle item`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(5, it) },
            equalTo(5))
    }

    @Test
    fun `binarySearch matches item in first half`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(2, it) },
            equalTo(2))
    }

    @Test
    fun `binarySearch matches item in second half`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(7, it) },
            equalTo(7))
    }

    @Test
    fun `binarySearch finds insertion point before start`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(-1, it) },
            equalTo(0.inv()))
    }

    @Test
    fun `binarySearch finds insertion point after end`() {
        assertThat(
            binarySearch((0..10).toList()) { compareValues(11, it) },
            equalTo(11.inv()))
    }

    @Test
    fun `binarySearch finds insertion point between items`() {
        assertThat(
            binarySearch((0..10).map { it.toDouble()}.toList()) { compareValues(3.5, it) },
            equalTo(4.inv()))
    }
}