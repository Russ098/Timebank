package it.polito.timebank.model

import com.google.firebase.firestore.DocumentSnapshot

data class TimeSlotData(var id:String?,
                   var title : String? ,
                   var description: String?,
                   var date: String?,
                   var time: String?,
                   var duration: String?,
                   var location: String?,
                   var others: String,
                   var skill: String,
                var email: String,
                var availability: Boolean?)

fun DocumentSnapshot.toTimeSlotData(): TimeSlotData {
    return TimeSlotData(
        this.id,
        this.get("title").toString(),
        this.get("description").toString(),
        this.get("date").toString(),
        this.get("time").toString(),
        this.get("duration").toString(),
        this.get("location").toString(),
        this.get("others").toString(),
        this.get("skills").toString(),
        this.get("email").toString(),
        this.get("availability") as Boolean?
    )
}