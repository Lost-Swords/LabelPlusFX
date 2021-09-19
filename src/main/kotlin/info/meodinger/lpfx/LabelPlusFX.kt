package info.meodinger.lpfx

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Options
import info.meodinger.lpfx.util.dialog.initDialogOwner
import info.meodinger.lpfx.util.resource.ICON
import info.meodinger.lpfx.util.resource.INFO
import info.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */

/**
 * LPFX Application
 */
class LabelPlusFX: Application() {

    init {
        Options.load()
        Logger.info("App start", "Application")
        State.application = this
    }

    override fun start(primaryStage: Stage) {
        State.stage = primaryStage

        val loader = FXMLLoader(javaClass.getResource("Window.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<Controller>()

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, WIDTH, HEIGHT)
        primaryStage.setOnCloseRequest {
            controller.close()
            it.consume()
        }
        primaryStage.show()

        State.controller = controller
        initDialogOwner(primaryStage)
    }
}