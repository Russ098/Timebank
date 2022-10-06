package it.polito.timebank.viewmodels

import android.app.Application
import android.content.ClipData
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import it.polito.timebank.model.TimeSlotData
import it.polito.timebank.model.toTimeSlotData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimeSlotViewModel(application: Application) : AndroidViewModel(application) {
    private val _timeSlots = MutableLiveData<List<TimeSlotData>>()
    val timeSlots : LiveData<List<TimeSlotData>> = _timeSlots

    fun getAllTimeSlot(){
        FirebaseFirestore
            .getInstance()
            .collection("advertisements")
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d -> d.toTimeSlotData() }
                }
            }
    }

    fun getTimeSlotByEmail(email: String){
        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("email", email)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d -> d.toTimeSlotData() }
                }
            }
    }

    fun getTimeSlotDateOrdered(order: Query.Direction){

        for(x in timeSlots.value!!){
            Log.d("TimeSlot", "prima l'order ${x.title}")
        }

        var sortedList = timeSlots.value
        if(order == Query.Direction.ASCENDING){
            sortedList = timeSlots.value!!.sortedWith(compareBy { it.date })
        }else{
            sortedList = timeSlots.value!!.sortedWith(compareBy { it.date }).reversed()
        }

        _timeSlots.value = sortedList!!
        for(x in timeSlots.value!!){
            Log.d("TimeSlot", "dopo l'order ${x.title}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeSlotOfToday(filter: String, email:String){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayFormatted = current.format(formatter)

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("skills", filter)
            .whereEqualTo("date", todayFormatted)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeSlotOfTomorrow(filter: String, email:String) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayFormatted = current.format(formatter)
        val tomorrowFormatted = current.plusDays(1).format(formatter)

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("skills", filter)
            .whereEqualTo("date", tomorrowFormatted)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeSlotOfThisWeek(filter: String, email: String){
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayFormatted = current.format(formatter)
        val nextWeekFormatted = current.plusWeeks(1).format(formatter)

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("skills", filter)
            .whereGreaterThanOrEqualTo("date", todayFormatted)
            .whereLessThan("date", nextWeekFormatted)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeSlotOfThisMonth(filter: String, email:String){
        val timeSlotTemp = MutableLiveData<List<TimeSlotData>>()

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayFormatted = current.format(formatter)
        val nextMonthFormatted = current.plusMonths(1).format(formatter)

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("skills", filter)
            .whereGreaterThanOrEqualTo("date", todayFormatted)
            .whereLessThan("date", nextMonthFormatted)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }

    }

    fun getTimeSlotById(id: String): MutableLiveData<TimeSlotData> {
        val timeslot = MutableLiveData<TimeSlotData>()
        FirebaseFirestore.getInstance().collection("advertisements")
            .document(id)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    timeslot.value = r.toTimeSlotData()
                }
            }

        return timeslot
    }

    fun updateTimeSlot(new_value:String, field:String, id:String){
        var db = FirebaseFirestore.getInstance()

        if(field == "skills") {
            var skillsArray: List<String> = new_value.split(",")
            val skills = skillsArray[0]
            db.collection("advertisements")
                .document(id)
                .update(field, skills)
                .addOnSuccessListener {
                    Log.d("CloudFirestore", "updateTimeSlot: UPDATE SUCCESSFUL")

                }.addOnFailureListener{
                    Log.d("CloudFirestore", "updateTimeSlot: UPDATE FAILED")
                }
        } else {
            db.collection("advertisements")
                .document(id)
                .update(field, new_value)
                .addOnSuccessListener {
                    Log.d("CloudFirestore", "updateTimeSlot: UPDATE SUCCESSFUL")
                }.addOnFailureListener{
                    Log.d("CloudFirestore", "updateTimeSlot: UPDATE FAILED")
                }
        }
    }

    fun addAdv(title: String?, description: String?, date: String?,
               time: String?, duration: String?, location: String?,
               others: String, email: String, skills: String, availability: Boolean){

        val skillsList: List<String> = skills.split(",")
        val skill = skillsList[0]

        val timeslot = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "duration" to duration,
            "location" to location,
            "others" to others,
            "email" to email,
            "skills" to skill,
            "availability" to availability
        )

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .add(timeslot)
            .addOnSuccessListener { documentReference ->
                Log.d("CloudFirestore", "addAdv: DocumentSnapshot added with ID: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w("CloudFirestore", "addAdv: Error adding document", e)
            }
    }

    fun getTimeSlotBySkill(skill: String, email:String): MutableLiveData<List<TimeSlotData>>{
        val timeSlotData = MutableLiveData<List<TimeSlotData>>()
        FirebaseFirestore.getInstance().collection("advertisements")
            .whereEqualTo("skills", skill)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    timeSlotData.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                    Log.d("CloudFirestore", "getTimeSlotByEmail: ${r.documents.toString()}")
                }
            }
        return timeSlotData
    }

    fun setTimeSlotBySkill(skill: String, email:String){
        FirebaseFirestore.getInstance().collection("advertisements")
            .whereEqualTo("skills", skill)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }
    }

    fun getTimeSlotByLocation(location: String?, skill: String, email:String){
        Log.d("location", "Get adv of: $location")

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("location", location)
            .whereEqualTo("skills", skill)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    _timeSlots.value = r.documents.mapNotNull { d ->
                        if (d.get("email") != email) { d.toTimeSlotData() }
                        else null
                    }
                }
            }

    }

    fun deleteAdvertisementById(id: String) {

        FirebaseFirestore.getInstance().collection("advertisements")
            .document(id)
            .delete()
    }

}
