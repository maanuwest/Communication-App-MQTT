package fhnw.emoba.thatsapp.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(var screenName: String, var icon: ImageVector) {
    ChatsOverview("Chats", icon = Icons.Filled.Dashboard),
    Contacts("Kontakte", icon = Icons.Filled.SupervisedUserCircle),
    Chat("Chat", icon = Icons.Filled.Leaderboard),
    Profile("Profil", icon = Icons.Filled.AddTask)
}