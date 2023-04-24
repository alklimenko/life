package ru.avklimenko.life

class FPS {
    private var ticks: Int = 0
    private var oldTime = Long.MIN_VALUE
    private var duration = 0L
    private var suspended = false


    fun tick() {
        ticks++
        suspended = false
        val time = System.nanoTime()
        if (oldTime != Long.MIN_VALUE) {
            duration += time - oldTime
        }
        oldTime = time
    }

    fun suspend() {
        oldTime = Long.MIN_VALUE
        suspended = true
    }

    fun resume() {
        if (suspended) {
            suspended = false
            oldTime = System.nanoTime()
        }
    }

    fun fps(): Double {
        return ticks * 1E9 / duration
    }

    fun init() {
        ticks = 0
        oldTime = Long.MIN_VALUE
        duration = 0
        suspended = false
    }
}
