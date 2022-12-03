package fhnw.emoba.thatsapp.data

import org.json.JSONObject
import java.net.URL

data class Profile(val uuid: String,
                   val time: Long,
                   var username: String,
                   val avatar: Image,
                   var status: String
                   ) {
    constructor(jsonObject: JSONObject) : this(
        uuid = jsonObject.getString("uuid"),
        time = jsonObject.getLong("time"),
        username = jsonObject.getString("username"),
        avatar = Image(url = jsonObject.getString("avatar")),
        status = jsonObject.getString("status"),
    )

    fun asJson(): String{
        return """
        {
        "uuid": "$uuid",
        "time": "$time",
        "username": "$username",
        "avatar": "${avatar.url}",
        "status": "$status"
        }
        """.trimIndent()
    }
}