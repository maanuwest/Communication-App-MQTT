package fhnw.emoba.thatsapp

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import fhnw.emoba.EmobaApp
import fhnw.emoba.modules.module09.gps.data.GPSConnector
import fhnw.emoba.modules.module09.photobooth.data.CameraAppConnector
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.AppUI


object ThatsApp : EmobaApp {
    private lateinit var model: ThatsAppModel

    override fun initialize(activity: ComponentActivity) {
        val gps = GPSConnector(activity)
        val cameraAppConnector = CameraAppConnector(activity)
        model = ThatsAppModel(activity = activity, gps, cameraAppConnector)
        model.initApp()
    }

    @Composable
    override fun CreateUI() {
        AppUI(model)
    }

}

