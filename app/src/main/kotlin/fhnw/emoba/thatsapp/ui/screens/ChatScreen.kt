package fhnw.emoba.thatsapp.ui.screens

import android.text.Layout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import fhnw.emoba.thatsapp.data.Image
import fhnw.emoba.thatsapp.data.ImageMessage
import fhnw.emoba.thatsapp.data.LocationMessage
import fhnw.emoba.thatsapp.data.TextMessage
import fhnw.emoba.thatsapp.model.ThatsAppModel
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun ChatScreen(model: ThatsAppModel){
    var textField by remember { mutableStateOf(model.currentText) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val mUriHandler = LocalUriHandler.current
    Column(modifier = Modifier
        .padding(10.dp)
        .fillMaxHeight(0.9f)
        .verticalScroll(scrollState)) {
        for (message in model.chats[model.activeChatPartner.uuid]!!){
            Column(Modifier.fillMaxWidth(1f)) {
                if(message.sender == model.myUUID){
                    Row(modifier = Modifier
                        .align(Alignment.End)
                        .fillMaxWidth(0.8f)) {
                        Text(text = model.myUsername, fontWeight = FontWeight.Bold)
                        Text(
                            text = model.getDateTime(message.time)!!,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }else{
                    Row() {
                        Text(
                            text = model.contacts[model.activeChatPartner.uuid]!!.username ,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = model.getDateTime(message.time)!!,
                            Modifier.padding(start = 10.dp)
                        )
                    }
                }
                when (message) {
                    is TextMessage ->
                        if(message.sender == model.myUUID){
                            Card(modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .align(Alignment.End),backgroundColor = model.buttonColor) {
                                Text(text = message.payload, Modifier.padding(3.dp))
                            }
                        }else{
                            Card(modifier = Modifier.fillMaxWidth(0.8f)) {
                                Text(text = message.payload, Modifier.padding(3.dp))
                        }
                    }
                    is LocationMessage ->
                        if(message.sender == model.myUUID){
                            Card(modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .align(Alignment.End), backgroundColor = model.buttonColor){
                                IconButton(onClick = {
                                    println("--------------------------------------https://www.openstreetmap.org/?mlat=${message.payload.latitude}&mlon=${message.payload.longitude}#map=17/${message.payload.latitude}/${message.payload.longitude}")
                                    mUriHandler.openUri("https://www.openstreetmap.org/?mlat=${message.payload.latitude}&mlon=${message.payload.longitude}#map=17/${message.payload.latitude}/${message.payload.longitude}")}) {
                                    Row(){
                                        Text(text = "Standort ")
                                        Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "")
                                    }
                                }
                            }
                        }else{
                            Card(modifier = Modifier.fillMaxWidth(0.8f)){
                                IconButton(onClick = {
                                    println("--------------------------------------https://www.openstreetmap.org/?mlat=${message.payload.latitude}&mlon=${message.payload.longitude}#map=17/${message.payload.latitude}/${message.payload.longitude}")
                                    mUriHandler.openUri("https://www.openstreetmap.org/?mlat=${message.payload.latitude}&mlon=${message.payload.longitude}#map=17/${message.payload.latitude}/${message.payload.longitude}")}, modifier = Modifier.background(MaterialTheme.colors.primary)) {
                                    Row(){
                                        Text(text = "Standort ")
                                        Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "")
                                    }
                                }
                            }
                        }
                    is ImageMessage ->
                        if(message.sender == model.myUUID){
                            Card(modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .align(Alignment.End),backgroundColor = model.buttonColor){
                                Image(bitmap = message.payload.image.asImageBitmap(), contentDescription = "")
                            }
                        }else{
                            Card(modifier = Modifier
                                .fillMaxWidth(0.8f)
                                ){
                                Image(bitmap = message.payload.image.asImageBitmap(), contentDescription = "")
                            }
                        }
                }
            }
        }
    }
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxHeight()) {
        Column(){
            Row(){
                IconButton(onClick = { model.sendLocation(model.activeChatPartner.uuid) }, modifier = Modifier.background(MaterialTheme.colors.primary)) {
                    Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "")
                }
                IconButton(onClick = { model.takePhoto(model.activeChatPartner.uuid) }, modifier = Modifier.background(MaterialTheme.colors.primary)) {
                    Icon(imageVector = Icons.Filled.Image, contentDescription = "")
                }
            }
            Row(Modifier.zIndex(100f)){
                OutlinedTextField(
                    value = textField,
                    onValueChange = { textField = it
                        model.currentText = textField
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        model.currentText = textField
                    })
                )
                IconButton(onClick = {
                    val textMessage = TextMessage("1","PLAINTEXT",model.myUUID,model.activeChatPartner.uuid,System.currentTimeMillis(),model.currentText)
                    model.sendMessage(model.activeChatPartner.uuid,textMessage.asJson())
                    model.updateChat(model.activeChatPartner.uuid,textMessage)
                    textField = ""
                }, Modifier.background(MaterialTheme.colors.primary)) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = "")
                }
            }
        }
    }
}