package ru.avklimenko.life

import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import kotlin.math.ceil


class Life {
    private val grid =  Array(1000) { ByteArray(1000) }
    private val oldGrid =  Array(1000) { ByteArray(1000) }

    companion object {
        private const val DEAD: Int = (0xff000000).toInt()
        private const val LIVE: Int = (0xff00ff00).toInt()
        private val COLOR = intArrayOf(DEAD, LIVE)
        private const val N_THREADS = 6
    }

    init {
        init(0.2)
    }

    fun init(p: Double) {
        for (y in 0..999) {
            for (x in 0..999) {
                grid[y][x] = if (y in 334..665 && x in 334.. 665) (if (Math.random() < p) 1 else 0) else 0
                oldGrid[y][x] = 0
            }
        }
    }

    fun step() {
        for (y in 0..999) {
            grid[y].copyInto(oldGrid[y])
        }

        // центральная часть (многопоточно)
        val threads = mutableListOf<Thread>()
        for (i in 0 until N_THREADS) {
            val t = Thread {
                for (y in i + 1..998 step N_THREADS) {
                    for (x in 1..998) {
                        val n = oldGrid[y - 1][x - 1] + oldGrid[y - 1][x] + oldGrid[y - 1][x + 1] +
                                oldGrid[y][x - 1] + oldGrid[y][x + 1] +
                                oldGrid[y + 1][x - 1] + oldGrid[y + 1][x] + oldGrid[y + 1][x + 1]
                        grid[y][x] = if (n == 3 || (n == 2 && oldGrid[y][x] == (1).toByte())) 1 else 0
                    }
                }
            }
            threads.add(t)
            t.start()
        }

        // границы

        // вертикальные
        for (y in 1..998) {
            // левая граница
            val n1 = oldGrid[y - 1][999] + oldGrid[y - 1][0] + oldGrid[y - 1][1] +
                    oldGrid[y][999] + oldGrid[y][1] +
                    oldGrid[y + 1][999] + oldGrid[y + 1][0] + oldGrid[y + 1][1]
            grid[y][0] = if (n1 == 3 || (n1 == 2 && oldGrid[y][0] == (1).toByte())) 1 else 0

            // правая граница
            val n2 = oldGrid[y - 1][998] + oldGrid[y - 1][999] + oldGrid[y - 1][0] +
                    oldGrid[y][998] + oldGrid[y][0] +
                    oldGrid[y + 1][998] + oldGrid[y + 1][999] + oldGrid[y + 1][0]
            grid[y][999] = if (n2 == 3 || (n2 == 2 && oldGrid[y][999] == (1).toByte())) 1 else 0
        }

        // горизонтальные
        for (x in 1..998) {
            // верхняя граница
            val n3 = oldGrid[999][x - 1] + oldGrid[999][x] + oldGrid[999][x + 1] +
                    oldGrid[0][x - 1] + oldGrid[0][x + 1] +
                    oldGrid[1][x - 1] + oldGrid[1][x] + oldGrid[1][x + 1]
            grid[0][x] = if (n3 == 3 || (n3 == 2 && oldGrid[0][x] == (1).toByte())) 1 else 0

            // нижняя граница
            val n4 = oldGrid[998][x - 1] + oldGrid[998][x] + oldGrid[998][x + 1] +
                    oldGrid[999][x - 1] + oldGrid[999][x + 1] +
                    oldGrid[0][x - 1] + oldGrid[0][x] + oldGrid[0][x + 1]
            grid[999][x] = if (n4 == 3 || (n4 == 2 && oldGrid[999][x] == (1).toByte())) 1 else 0
        }

        // углы
        // 0 0
        val n5 = oldGrid[999][999] + oldGrid[999][0] + oldGrid[999][1] +
                oldGrid[0][999] + oldGrid[0][1] +
                oldGrid[1][999] + oldGrid[1][0] + oldGrid[1][1]
        grid[0][0] = if (n5 == 3 || (n5 == 2 && oldGrid[0][0] == (1).toByte())) 1 else 0

        // 999 0
        val n6 = oldGrid[998][999] + oldGrid[998][0] + oldGrid[998][1] +
                oldGrid[999][999] + oldGrid[999][1] +
                oldGrid[0][999] + oldGrid[0][0] + oldGrid[0][1]
        grid[999][0] = if (n6 == 3 || (n6 == 2 && oldGrid[999][0] == (1).toByte())) 1 else 0

        // 0 999
        val n7 = oldGrid[999][998] + oldGrid[999][999] + oldGrid[999][0] +
                oldGrid[0][998] + oldGrid[0][0] +
                oldGrid[1][998] + oldGrid[1][999] + oldGrid[1][0]
        grid[0][999] = if (n7 == 3 || (n7 == 2 && oldGrid[0][999] == (1).toByte())) 1 else 0

        // 999 999
        val n8 = oldGrid[998][998] + oldGrid[998][999] + oldGrid[998][0] +
                oldGrid[999][998] + oldGrid[999][0] +
                oldGrid[0][998] + oldGrid[0][999] + oldGrid[0][0]
        grid[999][999] = if (n8 == 3 || (n8 == 2 && oldGrid[999][999] == (1).toByte())) 1 else 0

        threads.forEach { t -> t.join() }
    }

    private val buffer = IntArray(1000*1000)

    fun setImage(wi: WritableImage) {
        val pw = wi.pixelWriter

        val threads = mutableListOf<Thread>()
        val delta = ceil(1000.0 / N_THREADS).toInt()
        for (i in 0 until N_THREADS) {
            val t = Thread {
                val y0 = i * delta
                val yMax = (y0 + delta - 1).coerceAtMost(999)
                for (y in y0..yMax) {
                    val offset = y * 1000
                    for (x in 0..999) {
                        buffer[offset + x] = COLOR[grid[y][x].toInt()]
                    }
                }
                pw.setPixels(0, y0, 1000, yMax - y0 + 1, PixelFormat.getIntArgbInstance(), buffer, y0 * 1000, 1000)
            }
            threads.add(t)
            t.start()
        }
        threads.forEach { t -> t.join() }
    }
}
