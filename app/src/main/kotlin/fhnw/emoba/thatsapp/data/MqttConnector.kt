package fhnw.emoba.thatsapp.data

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import fhnw.emoba.thatsapp.model.ThatsAppModel
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * ACHTUNG: Das ist nur eine erste Konfiguration eines Mqtt-Brokers.
 *
 * Dient vor allem dazu mit den verschiedenen Parametern experimentieren zu können
 *
 * siehe die Doku:
 * https://hivemq.github.io/hivemq-mqtt-client/
 * https://github.com/hivemq/hivemq-mqtt-client
 *
 * Ein generischer Mqtt-Client (gut, um Messages zu kontrollieren)
 * http://www.hivemq.com/demos/websocket-client/
 *
 */
class MqttConnector (
    mqttBroker: String,
    val qos: MqttQos = MqttQos.EXACTLY_ONCE
){
    private val client = Mqtt5Client.builder()
        .serverHost(mqttBroker)
        .identifier(UUID.randomUUID().toString())
        .buildAsync()

    fun connect(onConnectionFailed: (Throwable) -> Unit = {}, onConnectionEstablished: () -> Unit = {}) {
        client.connectWith()
            .cleanStart(false)
            .keepAlive(30)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onConnectionFailed(throwable)
                } else { //erst wenn die Connection aufgebaut ist, kann subscribed werden
                    println("Connection to broker established...")
                    onConnectionEstablished()
                }
            }
    }

    fun subscribe(topic:        String,
                  onNewMessage: (JSONObject) -> Unit, onConnectionFailed: (Throwable) -> Unit = {}, onError: (Exception, String) -> Unit = { e, _ -> e.printStackTrace() }){
        client.subscribeWith()
            .topicFilter(topic)
            .qos(qos)
            .noLocal(true)
            .callback {
                println(it.payload)
                try {
                    onNewMessage(it.payloadAsJSONObject())
                } catch (e: Exception) {
                    onError(e, it.payloadAsString())
                }
            }
            .send()
    }

    fun publishProfile(topic:       String,
                message:     String,
                onPublished: () -> Unit = {},
                onError:     () -> Unit = {}) {
        client.publishWith()
            .topic(topic)
            .payload(message.asPayload())
            .qos(qos)
            .retain(true)  //Message soll nicht auf dem Broker gespeichert werden
            .noMessageExpiry()
            .send()
            .whenComplete {_, throwable ->
                if(throwable != null){
                    onError()
                }
                else {
                    onPublished()
                }
             }
    }

    fun publishMessage(topic:       String,
                       message:     String,
                       onPublished: () -> Unit = {},
                       onError:     () -> Unit = {}) {
        client.publishWith()
            .topic(topic)
            .payload(message.asPayload())
            .qos(qos)
            .retain(true)  //Message soll nicht auf dem Broker gespeichert werden
            .noMessageExpiry()
            .send()
            .whenComplete {_, throwable ->
                if(throwable != null){
                    onError()
                }
                else {
                    onPublished()
                }
            }
    }

    fun disconnect() {
        client.disconnectWith()
            .sessionExpiryInterval(0)
            .send()
    }
}

// praktische Extension Functions
private fun String.asPayload() : ByteArray = toByteArray(StandardCharsets.UTF_8)
private fun Mqtt5Publish.payloadAsString() : String = String(payloadAsBytes, StandardCharsets.UTF_8)
private fun Mqtt5Publish.payloadAsJSONObject(): JSONObject = JSONObject(payloadAsString())
private fun Profile.asPayload(): ByteArray = asJson().asPayload()