package ru.avklimenko.life

import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlin.system.exitProcess

class App : Application() {
    override fun start(stage: Stage) {
        stage.onCloseRequest = EventHandler { _: WindowEvent? ->
            Platform.exit()
            exitProcess(0)
        }
        val fxmlLoader = FXMLLoader(App::class.java.getResource("app.fxml"))
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }
}

fun main() {
    Application.launch(App::class.java)
}