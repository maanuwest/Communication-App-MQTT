package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.data.ImageMessage
import fhnw.emoba.thatsapp.data.LocationMessage
import fhnw.emoba.thatsapp.data.TextMessage
import fhnw.emoba.thatsapp.model.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatsOverview(model: ThatsAppModel){
    Column(
        modifier = Modifier.padding(10.dp).fillMaxWidth(1f).fillMaxHeight(1f)
    ) {
        if(model.chats.isEmpty()){
            Column() {
                Text(text = "Du hast keine offenen Chats.")
            }
        }else{
            Column(){
                for(chat in model.chats) {
                    if (chat.value.isNotEmpty()){
                        Card(
                            modifier = Modifier
                                .fillMaxWidth().padding(bottom = 10.dp),
                            onClick = {
                                model.activeChatPartner = model.contacts[chat.key]!!
                                model.activeScreen = Screens.Chat
                            }) {
                            Column() {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(bitmap = (model.contacts[chat.key]!!.avatar.image.asImageBitmap()),null,modifier = Modifier
                                        .size(64.dp).clip(shape = RoundedCornerShape(10.dp)).padding(5.dp)
                                    )
                                    Column(modifier = Modifier.padding(start = 25.dp)) {
                                        Text(text = model.contacts[chat.key]!!.username, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { model.activeScreen = Screens.Contacts },
            modifier = Modifier.align(Alignment.End).padding(top= 10.dp),
            content = {Icon(imageVector = Icons.Default.Add,"")},
            contentColor = model.buttonColor
        )
    }
}