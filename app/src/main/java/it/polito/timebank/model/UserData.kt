package it.polito.timebank.model

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

class UserData(var fullname : String? ,
               var nickname: String?,
               var email: String?,
               var location: String?,
               var skills: String?,
               var description: String?,
               var profileImage: String)

fun DocumentSnapshot.toUserData(): UserData {
    return UserData(
        this.get("fullname").toString(),
        this.get("nickname").toString(),
        this.get("email").toString(),
        this.get("location").toString(),
        this.get("skills").toString(),
        this.get("description").toString(),
        this.get("profileImage").toString(),
    )
}