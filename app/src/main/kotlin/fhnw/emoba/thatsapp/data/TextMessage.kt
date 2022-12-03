package fhnw.emoba.thatsapp.data

import org.json.JSONObject

class TextMessage(
    override val id: String,
    override val type: String,
    override val sender: String,
    override val receiver: String,
    override val time: Long,
    val payload: String
): Message(id, type, sender, receiver, time) {
    constructor(jsonObject: JSONObject) : this(
        id = jsonObject.getString("id"),
        type = jsonObject.getString("type"),
        sender = jsonObject.getString("sender"),
        receiver = jsonObject.getString("receiver"),
        time = jsonObject.getLong("time"),
        payload = jsonObject.getJSONObject("payload").getString("message")
    )

    fun asJson(): String{
        return """
        {
        "id": "$id",
        "type": "$type",
        "sender": "$sender",
        "receiver": "$receiver",
        "time": "$time",
        "payload": {"message": "$payload"}
        }
        """.trimIndent()
    }
}