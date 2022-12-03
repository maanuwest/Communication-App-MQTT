package fhnw.emoba.thatsapp.data

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.net.URL

data class Image(val url: String) {
    var image by mutableStateOf(
        Bitmap.createBitmap(
            120,
            120,
            Bitmap.Config.ALPHA_8
        ))
}