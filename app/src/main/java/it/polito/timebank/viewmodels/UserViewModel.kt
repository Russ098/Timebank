package it.polito.timebank.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import it.polito.timebank.model.UserData
import it.polito.timebank.model.toUserData

class UserViewModel(application: Application) : AndroidViewModel(application){
    private val _users = MutableLiveData<List<UserData>>()
    val user: LiveData<List<UserData>> = _users
    private val l: ListenerRegistration =
        FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { r, e ->
                if(r != null){
                    _users.value = if (e!=null)
                        emptyList()
                    else r.mapNotNull { d -> d.toUserData() }
                }
            }

    fun getUserByEmail(email: String): LiveData<UserData> {
        val user = MutableLiveData<UserData>()

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .addSnapshotListener { r, _ ->
                if (r != null) {
                    Log.d("CloudFirestore", r.documents[0].get("fullname").toString())
                    user.value = r.documents[0].toUserData()
                }
            }
        return user
    }


    fun updateProfile(new_value:String, field:String, email:String){
        var db = FirebaseFirestore.getInstance()

        if(field == "skills"){
            var skillsArray: List<String> = new_value.split(",")

            db.collection("users")
                .document(email)
                .update(field, skillsArray)
                .addOnSuccessListener {
                    Log.d("CloudFirestore", "updateProfile: UPDATE SUCCESSFUL")
                }.addOnFailureListener{
                    Log.d("CloudFirestore", "updateProfile: UPDATE FAILED")
                }
        } else {
            db.collection("users")
                .document(email)
                .update(field, new_value)
                .addOnSuccessListener {
                    Log.d("CloudFirestore", "updateProfile: UPDATE SUCCESSFUL")
                }.addOnFailureListener{
                    Log.d("CloudFirestore", "updateProfile: UPDATE FAILED")
                }
        }
    }

}