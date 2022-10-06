package it.polito.timebank.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class SkillsViewModel(application: Application) : AndroidViewModel(application) {

    fun addSkill(newSkills: String){

        val skillsList: List<String> = newSkills.split(",")
        val skill = skillsList[0]
        val skillTmp = hashMapOf(
            "description" to skill
        )

        FirebaseFirestore.getInstance().collection("skills")
            .document(skill)
            .set(skillTmp)
            .addOnSuccessListener { documentReference ->
                Log.d("CloudFirestore", "addSkill: DocumentSnapshot added with ID: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w("CloudFirestore", "addSkill: Error adding document", e)
            }

    }

    fun deleteSkill(skill: String) {
        FirebaseFirestore.getInstance().collection("skills")
            .document(skill)
            .delete()
    }
}

