package fhnw.emoba.thatsapp.ui.screens

import android.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactOverview(model: ThatsAppModel){
    Column(modifier = Modifier.padding(10.dp).fillMaxWidth(1f).fillMaxHeight(1f)){
        for(contact in model.contacts.values) {
            if(contact.uuid != model.myUUID){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    onClick = {
                         model.activeChatPartner = contact
                         model.activeScreen = Screens.Chat
                    }) {
                    Column() {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(bitmap = (contact.avatar.image.asImageBitmap()),null,modifier = Modifier
                                .size(64.dp).clip(shape = RoundedCornerShape(10.dp)).padding(5.dp)
                                )
                            Column(modifier = Modifier.padding(start = 25.dp)) {
                                Text(text = contact.username, fontWeight = FontWeight.Bold)
                                Text(text = contact.status)
                            }
                        }
                    }
                }
            }
        }
    }
}