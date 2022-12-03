package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.ThatsAppModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileOverview(model: ThatsAppModel){
    var usernameField by remember { mutableStateOf(model.myProfile.username) }
    var statusField by remember { mutableStateOf(model.myProfile.status) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Image(
        bitmap =  model.myProfile.avatar.image.asImageBitmap(),
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop,

        modifier = Modifier
            .graphicsLayer(
                shape = RoundedCornerShape(percent = 50),
                clip = true
            )
            .size(256.dp)
    )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usernameField,
            onValueChange = { usernameField = it
                model.myProfile.username = usernameField
                            },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                model.myProfile.username = usernameField
            }), modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = statusField,
            onValueChange = { statusField = it
                model.myProfile.status = statusField
                            },
            label = { Text("Status") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                model.myProfile.status = statusField
            }), modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            model.uploadProfile()
            },
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Speichern", color = Color.White)
        }

    }
}