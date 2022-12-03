package fhnw.emoba.thatsapp.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.R
import fhnw.emoba.modules.module09.gps.data.GPSConnector
import fhnw.emoba.modules.module09.photobooth.data.CameraAppConnector
import fhnw.emoba.thatsapp.data.*
import fhnw.emoba.thatsapp.data.gofileio.GoFileIOConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ThatsAppModel(val activity: ComponentActivity, private val locator: GPSConnector, private val cameraAppConnector: CameraAppConnector) {
    val mqttBroker = "broker.hivemq.com"
    private val mqttConnector by lazy { MqttConnector(mqttBroker) }
    private lateinit var goFileIOConnector: GoFileIOConnector
    private var goFileReady: Boolean = false

    val backgroundJob = SupervisorJob()
    val modelScope = CoroutineScope(backgroundJob + Dispatchers.IO)

    var activeScreen by mutableStateOf(Screens.ChatsOverview)

    var contacts = mutableMapOf<String,Profile>()
    var chats: Map<String,List<Message>> by mutableStateOf(emptyMap())

    val buttonColor = Color(35,98,243) //#2362F3


    lateinit var activeChatPartner: Profile

    var currentText = ""
    var photo by mutableStateOf<Bitmap?>(null)


    val myUUID = "e0ab20f4-b3e9-48b0-972f-c30a5b03f19d"
    var myUsername = "Manuel"
    var myStatus = "Hi I'm using ThatsApp!"
    var myParentId = ""
    lateinit var myProfile: Profile

    val profileTopic = "fhnw/emoba/fs22/thebest_thatsapp/users/+/profile"
    val conversationTopic = "fhnw/emoba/fs22/thebest_thatsapp/users/"+myUUID+"/conversations/#"


    fun initApp(){
        println("Initializing app...")
        modelScope.launch() {
            goFileIOConnector = GoFileIOConnector()
            goFileReady = true
            while(!goFileReady){}

            subscribeToProfiles()
            subscribeToChats()

            goFileIOConnector.uploadBitmapToGoFileIO(
                BitmapFactory.decodeResource(
                    activity.resources,
                    R.drawable.profile
                ), onSuccess = {
                    myParentId = it
                    myProfile = Profile(
                        myUUID,
                        System.currentTimeMillis(),
                        myUsername,
                        Image(myParentId),
                        myStatus
                    )

                    downloadImage(myProfile)
                })

                mqttConnector.connect(onConnectionEstablished = {
                    uploadProfile()
                });
        }
    }

    fun uploadProfile(){
        mqttConnector.publishProfile("fhnw/emoba/fs22/thebest_thatsapp/users/e0ab20f4-b3e9-48b0-972f-c30a5b03f19d/profile", myProfile.asJson(), onPublished = {println("----------------MyProfile published")}, onError = {println("---------publish failed")})
    }

    fun sendMessage(receiver: String, message: String){
        mqttConnector.publishMessage(message = message, topic = "fhnw/emoba/fs22/thebest_thatsapp/users/"+receiver+"/conversations/"+ myUUID)
    }

    fun downloadImage(profile: Profile){
        modelScope.launch(){
            goFileIOConnector.downloadBitmapFromGoFileIO(
                parentFolder = profile.avatar.url,
                onSuccess = {
                    profile.avatar.image = it
                }, onError = {
                    println("Failed to download image for parentId = " + profile.avatar.url)
                })
        }
    }

    fun updateChat(key: String, msg: Message){
        if (chats.containsKey(key)){
            val tempMap = chats.toMutableMap()
            val tempList = tempMap[key]!!.toMutableList()

            when(msg){
                is TextMessage -> tempList.add(msg)
                is LocationMessage -> tempList.add(msg)
                is ImageMessage -> tempList.add(msg)
            }

            tempMap[key] = tempList
            chats = tempMap
        }
    }

    fun getDateTime(l: Long): String? {
        try {
            val sdf = SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.GERMANY)
            val netDate = Date(l)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun subscribeToProfiles(){
        mqttConnector.subscribe(profileTopic,
            onNewMessage = {
                    it -> println("-------------------------------"+it)
                val profile = Profile(it)
                contacts[profile.uuid] = profile
                println("-----------Added Contact for " + profile.username)

                if (chats.containsKey(profile.uuid)){

                }else{
                    //add chat for new user
                    val tempList = chats.toMutableMap()
                    tempList[profile.uuid] = listOf()
                    chats = tempList
                    println("-----------Added Chat for " + profile.username)
                }

                downloadImage(contacts[profile.uuid]!!)
            },
            onConnectionFailed = {
                println("Subscribe to profile failed: "+ it.message)
            })
    }

    fun subscribeToChats(){
        mqttConnector.subscribe(conversationTopic,
            onNewMessage = {
                val sender = it.getString("sender")
                val payload: JSONObject = it.getJSONObject("payload")

                if(chats.contains(sender)){
                    if(payload.has("message")){
                        updateChat(sender,TextMessage(it))
                    }else if(payload.has("location")){
                        updateChat(sender,LocationMessage(it))
                    }else if(payload.has("image")){
                        val parentId = payload.getString("image")
                        val imageMessage = ImageMessage(it)
                        goFileIOConnector.downloadBitmapFromGoFileIO(parentId,onSuccess = {
                            imageMessage.payload.image = it
                        })
                        updateChat(sender,imageMessage)
                    }
                }else{
                    println("------Failed to save message with payload" + payload + ". Reason: Chat with sender "+ sender +" does not exist yet.")
                }
            },
            onConnectionFailed = {
                println("Subscribe to conversations failed: "+ it.message)
            })
    }

    fun sendLocation(receiver: String){
        modelScope.launch {
            locator.getLocation(onNewLocation = {
                println("--------------------"+it)
                val message = LocationMessage("1","GEOPOSITION",myUUID,receiver,System.currentTimeMillis(),it)
                val json = message.asJson()
                sendMessage(receiver,json)
                updateChat(receiver,message)
            },
                onFailure          = {},
                onPermissionDenied = {
                    "--------------------PERMISSION DENIED"
                },
            )
        }
    }

    fun takePhoto(receiver: String) {
        cameraAppConnector.getBitmap(
            onSuccess  = {
                photo = it.rotate(90f)
                modelScope.launch {
                    goFileIOConnector.uploadBitmapToGoFileIO(photo!!,onSuccess = {
                        val imageMessage = ImageMessage("1","IMAGE",myUUID,receiver,System.currentTimeMillis(),Image(it))
                        imageMessage.payload.image = photo
                        val json = imageMessage.asJson()
                        sendMessage(receiver,json)
                        updateChat(receiver,imageMessage)
                    })
                }
            },
            onCanceled = {})
    }

    private fun Bitmap.rotate(degrees: Float) : Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}