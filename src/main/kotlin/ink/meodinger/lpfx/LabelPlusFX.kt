package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * LPFX Application
 */
class LabelPlusFX: HookedApplication() {

    init {
        Options.load()

        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            Logger.exception(e)
            showException(e, State.stage)
        }

        State.application = this
    }

    override fun start(primaryStage: Stage) {
        State.stage = primaryStage

        var root: Parent
        var controller: Controller
        try {
            val loader = FXMLLoader(javaClass.getResource("Window.fxml"))
            root = loader.load()
            controller = loader.getController()
        } catch (_ : Exception) {
            root = View()
            controller = Controller(root)
        }

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT)
        primaryStage.setOnCloseRequest {
            controller.exit()
            it.consume()
        }

        State.controller = controller

        primaryStage.show()

        Logger.info("App start", LOGSRC_APPLICATION)
    }

    override fun stop() {
        State.stage.close()

        Logger.info("App stop", LOGSRC_APPLICATION)

        Options.save()

        runShutdownHooksAndExit()
    }
}