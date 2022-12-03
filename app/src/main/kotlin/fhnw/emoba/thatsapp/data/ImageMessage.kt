package fhnw.emoba.thatsapp.data

import org.json.JSONObject

data class ImageMessage(
    override val id: String,
    override val type: String,
    override val sender: String,
    override val receiver: String,
    override val time: Long,
    val payload: Image
): Message(id, type, sender, receiver, time) {
    constructor(jsonObject: JSONObject) : this(
        id = jsonObject.getString("id"),
        type = jsonObject.getString("type"),
        sender = jsonObject.getString("sender"),
        receiver = jsonObject.getString("receiver"),
        time = jsonObject.getLong("time"),
        payload = Image(
            url = jsonObject.getJSONObject("payload").getString("image")
        )
    )

    fun asJson(): String{
        return """
        {
        "id": "$id",
        "type": "$type",
        "sender": "$sender",
        "receiver": "$receiver",
        "time": "$time",
        "payload": {
            "image": "${payload.url}"
                }
            }
        }
        """.trimIndent()
    }
}