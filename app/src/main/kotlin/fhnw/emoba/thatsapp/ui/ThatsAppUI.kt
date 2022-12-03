package fhnw.emoba.thatsapp.ui


import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.model.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.screens.ChatScreen
import fhnw.emoba.thatsapp.ui.screens.ChatsOverview
import fhnw.emoba.thatsapp.ui.screens.ContactOverview
import fhnw.emoba.thatsapp.ui.screens.ProfileOverview

val colors = darkColors(
    primary = Color(22,24,36), //#161824

//    primary = Color(255,0,0), //#161824
    secondary = Color(38,42,54), //#262A36
)
val fontColor = Color(207,211,222) //#CFD3DE

@Composable
fun AppUI(model : ThatsAppModel){
    val scaffoldState = rememberScaffoldState()
    with(model){
        MaterialTheme(colors = colors){
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = { TopBar(model = model) },
                content = {
                    when (model.activeScreen) {
                        Screens.ChatsOverview -> {
                            ChatsOverview(model)
                        }
                        Screens.Contacts -> {
                            ContactOverview(model)
                        }
                        Screens.Profile -> {
                            ProfileOverview(model)
                        }
                        Screens.Chat -> {
                            ChatScreen(model)
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(model: ThatsAppModel) {
    TopAppBar(
        title = {
            if(model.activeScreen == Screens.Chat){
                Text(text = model.contacts[model.activeChatPartner.uuid]!!.username)
            }else{
                Text(text = model.activeScreen.screenName)
            }
        },
        navigationIcon = {
            if(model.activeScreen != Screens.ChatsOverview){
                IconButton(onClick = { model.activeScreen = Screens.ChatsOverview}) {
                    Icon(Icons.Filled.ArrowBack, "backIcon")
                }
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = fontColor,
        elevation = 10.dp,
        actions = {
            IconButton(onClick = { model.activeScreen = Screens.Profile }) {
                Icon(Icons.Filled.AccountCircle, "backIcon")
            }
        }
    )
}