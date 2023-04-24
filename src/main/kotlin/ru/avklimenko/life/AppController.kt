package ru.avklimenko.life

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.image.WritableImage
import java.util.concurrent.atomic.AtomicBoolean

class AppController {
    @FXML
    private lateinit var canvas: Canvas

    @FXML
    private lateinit var btnInit: Button

    @FXML
    private lateinit var btnRun: Button

    @FXML
    private lateinit var btnStop: Button

    private val life = Life()

    private val suspended = AtomicBoolean(false)
    private val threadSuspended = AtomicBoolean(false)
    private val fps = FPS()
    private var step = 0

    private var stepTime: Long = 0
    private var setImageTime: Long = 0
    private var drawImageTime: Long = 0
    private var totalTime: Long = 0
    private val wi = WritableImage(1000, 1000)

    private var run = false

    private val thread = Thread {
        while (true) {
            while (suspended.get()) {
                threadSuspended.set(true)
                try {
                    Thread.sleep(1)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
            threadSuspended.set(false)
            fps.tick()
            step++
            if (step % 100 == 0) {
                println("${step} :  ${fps.fps()} " +
                        "\t total: ${totalTime / 1E9} " +
                        "\t step: ${100.0 * stepTime / totalTime }% " +
                        "\t setImage: ${100.0 * setImageTime / totalTime}% " +
                        "\t drawImage: ${100.0 * drawImageTime / totalTime} %")
            }
            val st0 = System.nanoTime()
            life.step()
            stepTime += System.nanoTime() - st0
            val st1 = System.nanoTime()
            life.setImage(wi)
            setImageTime += System.nanoTime() - st1
            val st2 = System.nanoTime()
            canvas.graphicsContext2D.drawImage(wi, 0.0, 0.0)
            drawImageTime += System.nanoTime() - st2
            totalTime += System.nanoTime() - st0
        }
    }


    @FXML
    private fun run() {
        if (!run) {
            run = true
            life.init(0.2)
            thread.start()
        } else if (suspended.get()) {
            suspended.set(false)
        }
    }

    @FXML
    private fun stop() {
        suspended.set(true)
        fps.suspend()
    }

    @FXML
    private fun init() {
        suspended.set(true)
        while (!threadSuspended.get()) Thread.sleep(1)
        step = 0
        life.init(0.1)
        fps.init()
        suspended.set(false)
    }
}