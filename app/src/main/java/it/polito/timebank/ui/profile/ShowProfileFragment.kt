package it.polito.timebank.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.model.UserData
import it.polito.timebank.viewmodels.UserViewModel
import java.io.File

class ShowProfileFragment : Fragment() {
    val vm_user by viewModels<UserViewModel>()
    lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.show_profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val externalUserEmail = arguments?.getString("externalUserEmail").toString()
        val user: LiveData<UserData>
        val tv_email = view.findViewById<TextView>(R.id.email)
        val tv_name = view.findViewById<TextView>(R.id.fullName)
        val tv_nickname = view.findViewById<TextView>(R.id.nickname)
        val tv_location = view.findViewById<TextView>(R.id.location)
        val tv_skills = view.findViewById<TextView>(R.id.skills)
        val tv_description = view.findViewById<TextView>(R.id.description)
        val image = view.findViewById<ImageView>(R.id.imageView)

        if(externalUserEmail!="null"){
            (activity as MainActivity).supportActionBar?.title = "Profile Details"
            user = vm_user.getUserByEmail(externalUserEmail)
            setHasOptionsMenu(false)
        } else {
            (activity as MainActivity).supportActionBar?.title = "Your Profile"
            user = vm_user.getUserByEmail(Firebase.auth.currentUser?.email.toString())
            setHasOptionsMenu(true)
        }


        activity?.let { it -> user.observe(it){ tv_name.text = it.fullname } }
        activity?.let { it -> user.observe(it){ tv_nickname.text = it.nickname } }
        activity?.let { it -> user.observe(it){ tv_email.text = it.email } }
        activity?.let { it -> user.observe(it){ tv_location.text = it.location } }
        activity?.let { it -> user.observe(it){ tv_skills.text = it.skills } }
        activity?.let { it -> user.observe(it){ tv_description.text = it.description } }
        activity?.let { it ->
            user.observe(it) {
                storageReference = FirebaseStorage.getInstance().getReference(it.profileImage)
                Log.d("CloudFirestore", storageReference.toString())
                val localfile: File = File.createTempFile("tempfile", "jpg")
                storageReference.getFile(localfile)
                    .addOnSuccessListener {
                        val bitmap: Bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                        image.setImageBitmap(bitmap)
                        Log.d("CloudFirestore", "ShowProfile, onViewCreated -> success in retrieving profile picture")
                    }.addOnFailureListener {
                        Log.d("CloudFirestore", "ShowProfile, onViewCreated -> some error occurred while retrieving the picture")
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_edit_profile -> {
                findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}