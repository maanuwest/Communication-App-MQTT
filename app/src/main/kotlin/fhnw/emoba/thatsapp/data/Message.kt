package fhnw.emoba.thatsapp.data

import org.json.JSONObject

open class Message(
    open val id: String,
    open val type: String,
    open val sender: String,
    open val receiver: String,
    open val time: Long
){
    constructor(jsonObject: JSONObject) : this(
        id = jsonObject.getString("id"),
        type = jsonObject.getString("type"),
        sender = jsonObject.getString("sender"),
        receiver = jsonObject.getString("receiver"),
        time = jsonObject.getLong("time"),
    )
}