package org.balefrost.aoc2024.day14

import org.balefrost.aoc2024.LongXY
import org.balefrost.aoc2024.day13.lcm
import org.balefrost.aoc2024.readInputLines
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*
import kotlin.math.max
import kotlin.math.min

data class Robot(val p: LongXY, val v: LongXY)

data class LongRect(val xy: LongXY, val wh: LongXY) {
    val left get() = xy.x
    val top get() = xy.y
    val right get() = xy.x + wh.x
    val bottom get() = xy.y + wh.y
    operator fun contains(pos: LongXY): Boolean {
        return pos.x in left..<right && pos.y in top..<bottom
    }
}

fun computeSafety(positions: Iterable<LongXY>, dims: LongXY): Long {
    val quadrants = LongArray(4)
    val mid = (dims - LongXY(1, 1)) / 2
    for (p in positions) {
        var q = 0
        when {
            p.x < mid.x -> {}
            p.x > mid.x -> q += 1
            else -> q += 100
        }
        when {
            p.y < mid.y -> {}
            p.y > mid.y -> q += 2
            else -> q += 100
        }
        if (q > 3) continue
        ++quadrants[q]
    }
    return quadrants.reduce(Long::times)
}

object Day14Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day14.txt")
        val dims = LongRect(LongXY(0, 0), LongXY(101, 103))
        val lineRegex = """p=(-?\d+),(-?\d+) v=(-?\d+),(-?\d+)""".toRegex()

        val robots = lines.map { line ->
            val (x, y, vx, vy) = lineRegex.matchEntire(line)!!.groupValues.drop(1).map(String::toLong)
            Robot(LongXY(x, y), LongXY(vx, vy))
        }


        val movedRobots = robots.map { (p, v) ->
            var pp = p
            repeat(100) {
                pp = (pp + v + dims.wh) % dims.wh
            }
            Robot(pp, v)
        }

        val safety = computeSafety(movedRobots.asSequence().map { it.p }.asIterable(), dims.wh)
        println(safety)
    }
}

class FancyRobot private constructor(val p: LongXY, val v: LongXY, val dims: LongXY, val ticksToReset: Long) {
    constructor(p: LongXY, v: LongXY, dims: LongXY) : this(p, v, dims, findTicksToReset(v, dims))

    fun step(): FancyRobot {
        val pp = (p + v + dims) % dims
        return FancyRobot(pp, v, dims, ticksToReset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FancyRobot

        if (p != other.p) return false
        if (v != other.v) return false
        if (dims != other.dims) return false

        return true
    }

    override fun hashCode(): Int {
        var result = p.hashCode()
        result = 31 * result + v.hashCode()
        result = 31 * result + dims.hashCode()
        return result
    }

    companion object {
        fun findTicksToReset(v: LongXY, dims: LongXY): Long {
            val ticksToResetX = lcm(v.x, dims.x) / v.x
            val ticksToResetY = lcm(v.y, dims.y) / v.y
            return lcm(ticksToResetX, ticksToResetY)
        }
    }
}

object Day14Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day14.txt")
        val dims = LongRect(LongXY(0, 0), LongXY(101, 103))
        val lineRegex = """p=(-?\d+),(-?\d+) v=(-?\d+),(-?\d+)""".toRegex()

        val initialRobots = lines.map { line ->
            val (x, y, vx, vy) = lineRegex.matchEntire(line)!!.groupValues.drop(1).map(String::toLong)
            FancyRobot(LongXY(x, y), LongXY(vx, vy), dims.wh)
        }

        val cycleTime = initialRobots.fold(1L) { acc, robot -> lcm(acc, robot.ticksToReset) }

        val mid = (dims.wh - LongXY(1, 1)) / 2

        var robotSet = initialRobots

        data class Position(val time: Long, val positions: Set<LongXY>, val safety: Long)

        val positions = mutableListOf(
            Position(
                0,
                robotSet.mapTo(mutableSetOf()) { it.p },
                computeSafety(robotSet.mapTo(mutableListOf()) { it.p }, dims.wh)
            )
        )
        for (time in 1..cycleTime) {
            robotSet = robotSet.map { it.step() }
            val quadrants = LongArray(4)
            for (r in robotSet) {
                var q = 0
                when {
                    r.p.x < mid.x -> {}
                    r.p.x > mid.x -> q += 1
                    else -> q += 100
                }
                when {
                    r.p.y < mid.y -> {}
                    r.p.y > mid.y -> q += 2
                    else -> q += 100
                }
                if (q > 3) continue
                ++quadrants[q]
            }
            positions.add(
                Position(
                    time,
                    robotSet.mapTo(mutableSetOf()) { it.p },
                    computeSafety(robotSet.mapTo(mutableListOf()) { it.p }, dims.wh)
                )
            )
        }
//        positions.sortByDescending { it.positions.size }
        positions.sortBy { it.safety }
        println(positions[0].time)


        check(initialRobots == robotSet)
        val robotPane = object : JComponent() {
            val magnification = 4
            private val size = Dimension(dims.wh.x.toInt() * magnification, dims.wh.y.toInt() * magnification)
            override fun getPreferredSize(): Dimension = size
            override fun getMinimumSize(): Dimension = size
            override fun getMaximumSize(): Dimension = size

            override fun paintComponent(g: Graphics) {
                g.color = Color.WHITE
                g.fillRect(0, 0, size.width, size.height)
                g.color = Color.BLACK
                for (robot in robots) {
                    g.fillRect(
                        robot.x.toInt() * magnification,
                        robot.y.toInt() * magnification,
                        magnification,
                        magnification
                    )
                }
            }

            fun setRobots(robots: Iterable<LongXY>) {
                this.robots = robots.toList()
                repaint()
            }

            private var robots: List<LongXY> = emptyList()
        }
        robotPane.setRobots(positions[0].positions)

        var currentIndex = 0

        val indexLabel = JLabel().apply { horizontalAlignment = SwingConstants.CENTER }

        fun refreshIndexLabel() {
            indexLabel.text =
                "${currentIndex + 1} / ${positions.size} - ${positions[currentIndex].positions.size} / ${initialRobots.size} - ${positions[currentIndex].safety} - (${positions[currentIndex].time})"
        }

        fun moveToIndex(index: Int) {
            val modifiedIndex = max(0, min(positions.lastIndex, index))
            if (currentIndex == modifiedIndex) {
                return
            }
            currentIndex = modifiedIndex
            refreshIndexLabel()
            robotPane.setRobots(positions[currentIndex].positions)
        }

        fun moveStart() {
            moveToIndex(0)
        }

        fun movePrev() {
            moveToIndex(currentIndex - 1)
        }

        fun moveNext() {
            moveToIndex(currentIndex + 1)
        }

        fun moveEnd() {
            moveToIndex(positions.lastIndex)
        }

        refreshIndexLabel()

        val startButton = JButton("<<").apply {
            addActionListener {
                moveStart()
            }
        }
        val prevButton = JButton("<").apply {
            addActionListener {
                movePrev()
            }
        }
        val nextButton = JButton(">").apply {
            addActionListener {
                moveNext()
            }
        }
        val endButton = JButton(">>").apply {
            addActionListener {
                moveEnd()
            }
        }

        val buttonPane = JPanel()
        buttonPane.layout = GridBagLayout()
        val c = GridBagConstraints()
        c.gridy = 0

        c.gridx = 0
        buttonPane.add(startButton, c)

        c.gridx = 1
        buttonPane.add(prevButton, c)

        c.gridx = 2
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1.0
        buttonPane.add(indexLabel, c)

        c.gridx = 3
        c.fill = GridBagConstraints.NONE
        c.weightx = 0.0
        buttonPane.add(nextButton, c)

        c.gridx = 4
        buttonPane.add(endButton, c)
        val frame = JFrame()
        val contentPane = JPanel()
        frame.contentPane = contentPane
        frame.contentPane.layout = BorderLayout()
        frame.contentPane.add(buttonPane, BorderLayout.NORTH)
        frame.contentPane.add(robotPane, BorderLayout.CENTER)
        frame.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE

        contentPane.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "GoLeft")
        contentPane.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "GoRight")
        contentPane.actionMap.put("GoLeft", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                movePrev()
            }
        })
        contentPane.actionMap.put("GoRight", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                moveNext()
            }
        })

        frame.pack()
        frame.isVisible = true

    }
}
