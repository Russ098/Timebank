package it.polito.timebank.ui.chat

import com.google.firebase.firestore.DocumentSnapshot
import it.polito.timebank.model.TimeSlotData

data class Request(val sender: String,val receiver: String,val  title: String,val accepted: Boolean,val pending: Boolean)

fun DocumentSnapshot.toRequestData(): Request {
    return Request(
        this.get("sender").toString(),
        this.get("receiver").toString(),
        this.get("title").toString(),
        this.get("accepted") as Boolean,
        this.get("pending") as Boolean,
        )
}