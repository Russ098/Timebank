package it.polito.timebank.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.timebank.R
import it.polito.timebank.databinding.ActivityMainBinding

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = FirebaseAuth.getInstance()

        val button = requireView().findViewById<SignInButton>(R.id.btnGoogleSignin)
        val pleaseSignInTV = view.findViewById<TextView>(R.id.pleaseSignInTV)

        if(auth.currentUser!=null){
            button.visibility = View.INVISIBLE
            pleaseSignInTV.visibility = View.INVISIBLE
        } else {
            button.visibility = View.VISIBLE
            pleaseSignInTV.visibility = View.VISIBLE
        }

        button.setOnClickListener {
            signIn()
            Log.d(TAG, "CurrentUser:" + auth.currentUser)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.hide()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser != null){
            googleSignInClient.revokeAccess()

            userInitialization()

        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    fun userInitialization(){

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                var flagAlreadyRegistered = false

                for (document in result) {
//                    Log.d(TAG, "userInitialization: ${document.id} => ${document.data["email"]}")
                    if(document.data["email"] == Firebase.auth.currentUser?.email.toString()){
                        flagAlreadyRegistered = true
                    }
                }

                if(flagAlreadyRegistered){
                    Log.d(it.polito.timebank.ui.timeslot.TAG, "userInitialization: User ${Firebase.auth.currentUser?.email.toString()} is already registered")
                    (activity as AppCompatActivity).supportActionBar!!.show()
                    findNavController().navigate(R.id.action_loginFragment_to_skillFragment)
                } else {
                    Log.d(it.polito.timebank.ui.timeslot.TAG, "NEW USER ${Firebase.auth.currentUser?.email.toString()}. NEED TO REGISTER")
                    val user = hashMapOf(
                        "fullname" to Firebase.auth.currentUser?.displayName.toString(),
                        "nickname" to "Empty",
                        "email" to Firebase.auth.currentUser?.email.toString(),
                        "location" to "Empty",
                        "skills" to "Empty",
                        "description" to "Empty",
                        "profileImage" to "images/profile_image_default.jpg"
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.currentUser?.email.toString())
                        .set(user)
                        .addOnSuccessListener { documentReference ->
                            Log.d(it.polito.timebank.ui.timeslot.TAG, "DocumentSnapshot added with ID: $documentReference")
                        }
                        .addOnFailureListener { e ->
                            Log.w(it.polito.timebank.ui.timeslot.TAG, "Error adding document", e)
                        }
                    (activity as AppCompatActivity).supportActionBar!!.show()
                    findNavController().navigate(R.id.action_loginFragment_to_editProfileFragment)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(it.polito.timebank.ui.timeslot.TAG, "Error getting documents.", exception)
            }
    }

}