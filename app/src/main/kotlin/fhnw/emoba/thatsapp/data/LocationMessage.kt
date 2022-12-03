package fhnw.emoba.thatsapp.data

import org.json.JSONObject

data class LocationMessage(
    override val id: String,
    override val type: String,
    override val sender: String,
    override val receiver: String,
    override val time: Long,
    val payload: Location
): Message(id, type, sender, receiver, time) {
    constructor(jsonObject: JSONObject) : this(
        id = jsonObject.getString("id"),
        type = jsonObject.getString("type"),
        sender = jsonObject.getString("sender"),
        receiver = jsonObject.getString("receiver"),
        time = jsonObject.getLong("time"),
        payload = Location(
            longitude = jsonObject.getJSONObject("payload").getJSONObject("location").getDouble("longitude"),
            latitude = jsonObject.getJSONObject("payload").getJSONObject("location").getDouble("latitude"),
            altitude = jsonObject.getJSONObject("payload").getJSONObject("location").getDouble("altitude"))
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
            "location": {
                "longitude": "${payload.longitude}",
                "latitude": "${payload.latitude}",
                "altitude": "${payload.altitude}"
                }
            }
        }
        """.trimIndent()
    }
}