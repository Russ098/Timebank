package it.polito.timebank.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.timebank.model.TimeSlotData
import it.polito.timebank.ui.chat.Request
import it.polito.timebank.ui.chat.toRequestData


class ChatViewModel: ViewModel() {
    private val _pendingSend = MutableLiveData<List<Request>>()
    val pendingSend : LiveData<List<Request>> = _pendingSend

    private val _pendingReceived = MutableLiveData<List<Request>>()
    val pendingReceived : LiveData<List<Request>> = _pendingReceived

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var l:ListenerRegistration =
        db.collection("channel")
        .whereEqualTo("sender",Firebase.auth.currentUser?.email.toString())
        .addSnapshotListener { v, e ->
            if (e==null) {
                _pendingSend.value = v!!.mapNotNull { d -> d.toRequestData() }
            } else _pendingSend.value = emptyList()
        }

    init {

        l = db.collection("channel")
            .whereEqualTo("receiver",Firebase.auth.currentUser?.email.toString())
            .addSnapshotListener { v, e ->
                if (e==null) {
                    _pendingReceived.value = v!!.mapNotNull { d -> d.toRequestData() }
                } else _pendingReceived.value = emptyList()
            }
    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
    }

    fun sendRequest(sender: String, receiver: String, title: String, accepted: Boolean, pending: Boolean){
        val request = hashMapOf(
            "sender" to sender,
            "receiver" to receiver,
            "title" to title,
            "accepted" to accepted,
            "pending" to pending
        )
        FirebaseFirestore.getInstance()
            .collection("channel")
            .add(request)
            .addOnSuccessListener { documentReference ->
                Log.d("Chat", "Inviata richiesta per un servizio")
            }
            .addOnFailureListener { e ->
                Log.w("Chat", "addAdv: Error adding document", e)
            }

    }

    fun responseRequest(sender: String, receiver: String, accepted: Boolean){
        FirebaseFirestore.getInstance()
            .collection("channel")
            .whereEqualTo("sender", sender)
            .whereEqualTo("receiver", receiver)
            .addSnapshotListener{ document, _ ->
                if(document != null) {
                    FirebaseFirestore
                        .getInstance()
                        .collection("channel")
                        .document(document.toString())
                        .update("accepted", accepted)
                        .addOnSuccessListener {
                            Log.d("Chat", "updateProfile: UPDATE SUCCESSFUL")
                        }.addOnFailureListener {
                            Log.d("Chat", "updateProfile: UPDATE FAILED")
                        }
                }
            }

    }

}