package it.polito.timebank.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.model.UserData
import it.polito.timebank.viewmodels.UserViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.concurrent.thread


class EditProfileFragment : Fragment() {

    val vm_user by viewModels<UserViewModel>()
    var stringProfileImage = ""
    lateinit var storageReference: StorageReference

    var storageRef = Firebase.storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.edit_profile_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_edit_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_save_profile -> {
                saveChange()
            };
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    fun saveChange(): Boolean {
        val name = view?.findViewById<EditText>(R.id.fullNameEdit)
        val nickname = view?.findViewById<EditText>(R.id.nicknameEdit)
        val location = view?.findViewById<EditText>(R.id.locationEdit)
        val skills = view?.findViewById<EditText>(R.id.skillsEdit)
        val description = view?.findViewById<EditText>(R.id.descriptionEdit)

        hideKeyboard()

        if(checkFieldsOnSave(name, nickname, location, skills)){

            if (name?.text.toString().isNotEmpty())
                vm_user.updateProfile(name?.text.toString(), "fullname", Firebase.auth.currentUser?.email.toString())

            if (nickname?.text.toString().isNotEmpty())
                vm_user.updateProfile(nickname?.text.toString(), "nickname", Firebase.auth.currentUser?.email.toString())

            if (location?.text.toString().isNotEmpty())
                vm_user.updateProfile(location?.text.toString(), "location", Firebase.auth.currentUser?.email.toString())

            if (skills?.text.toString().isNotEmpty())
                vm_user.updateProfile(skills?.text.toString(), "skills", Firebase.auth.currentUser?.email.toString())

            if (description?.text.toString().isNotEmpty())
                vm_user.updateProfile(description?.text.toString(), "description", Firebase.auth.currentUser?.email.toString())
        }

        if (stringProfileImage.isNotEmpty()) {
            Log.d("CloudFirestore", "---------------- EdtiProfile: stringProfileImage: $stringProfileImage")
            uploadImageToFirebase(Uri.parse(stringProfileImage))
        }

        findNavController().popBackStack()


        return true;
    }

    fun checkFieldsOnSave(name: EditText?, nickname: EditText?, location: EditText?, skills: EditText?): Boolean{

        if( name?.text.toString().isNotEmpty()){
            name?.error = null
        } else {
            if(name?.hint.toString().isNotEmpty()){
                name?.error = null
            }else{
                name?.error = "Empty name"
            }
        }

        if( nickname?.text.toString().isNotEmpty()){
            nickname?.error = null
        } else {
            if(nickname?.hint.toString().isNotEmpty()){
                nickname?.error = null
            }else{
                nickname?.error = "Empty nickname"
            }
        }

        if( location?.text.toString().isNotEmpty()){
            location?.error = null
        } else {
            if(location?.hint.toString().isNotEmpty()){
                location?.error = null
            }else{
                location?.error = "Empty location"
            }
        }

        if( skills?.text.toString().isNotEmpty()){
            skills?.error = null
        } else {
            if(skills?.hint.toString().isNotEmpty()){
                skills?.error = null
            }else{
                skills?.error = "Empty skills"
            }
        }

        if(!name?.error.isNullOrEmpty() or !nickname?.error.isNullOrEmpty() or
                !location?.error.isNullOrEmpty() or !skills?.error.isNullOrEmpty()) return false
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Edit Your Profile"
        val name = view.findViewById<EditText>(R.id.fullNameEdit)
        val nickname = view.findViewById<EditText>(R.id.nicknameEdit)
        val email = view.findViewById<TextView>(R.id.emailEdit)
        val location = view.findViewById<EditText>(R.id.locationEdit)
        val skills = view.findViewById<EditText>(R.id.skillsEdit)
        val description = view.findViewById<EditText>(R.id.descriptionEdit)
        val image = view.findViewById<ImageView>(R.id.imageView)

        val user: LiveData<UserData> = vm_user.getUserByEmail(Firebase.auth.currentUser?.email.toString())

        activity?.let { it -> user.observe(it){ name.hint = it.fullname } }
        activity?.let { it -> user.observe(it){ nickname.hint = it.nickname } }
        activity?.let { it -> user.observe(it){ email.hint = it.email } }
        activity?.let { it -> user.observe(it){ location.hint = it.location } }
        activity?.let { it -> user.observe(it){ skills.hint = it.skills } }
        activity?.let { it -> user.observe(it){ description.hint = it.description } }
        activity?.let { it ->
            user.observe(it) {
                storageReference = FirebaseStorage.getInstance().getReference(it.profileImage)
                Log.d("CloudFirestore", storageReference.toString())
                val localfile: File = File.createTempFile("tempfile", "jpg")
                storageReference.getFile(localfile)
                    .addOnSuccessListener {
                        val bitmap: Bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                        image.setImageBitmap(bitmap)
                        Log.d("CloudFirestore", "EditProfileFragment: Success in retrieving image")
                    }.addOnFailureListener {
                        Log.d("CloudFirestore", "EditProfileFragment: Failed in retrieving image")
                    }
            }
        }

        val btnCamera = view.findViewById<ImageButton>(R.id.edit)
        btnCamera?.setOnClickListener {
            val popup = PopupMenu(context, btnCamera)
            popup.menuInflater.inflate(R.menu.camera_gallery, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val itemTitle = item.title.toString()
                when (itemTitle) {
                    "Camera picture" -> {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        resultLauncherCamera.launch(cameraIntent)
                    }
                    "Load image" -> {
                        val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT )
                        galleryIntent.setType("image/*");
                        resultLauncherGallery.launch(galleryIntent)
                    }
                }
                true
            }

            popup.show()

        }
    }

    private val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.data
                if (bitmap != null) {
                    activity?.contentResolver?.takePersistableUriPermission(bitmap,  Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val profileImage = view?.findViewById<ImageView>(R.id.imageView)
                profileImage?.setImageURI(bitmap)
                stringProfileImage = context?.let {result.data?.data}.toString()
            }
        }

    private val resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("LOG", result.data.toString())
                handleCameraImage(result.data)
            }
        }

    private fun handleCameraImage(intent: Intent?) {
        val bitmap = intent?.extras?.get("data") as Bitmap
        val profileImage = view?.findViewById<ImageView>(R.id.imageView)
        profileImage?.setImageBitmap(bitmap)
        var uriProfileImage = context?.let { getImageUri(it, bitmap)}
        stringProfileImage = uriProfileImage.toString()
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }


    private fun uploadImageToFirebase(fileUri: Uri) {
        val fileName = UUID.randomUUID().toString() +".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("${Firebase.auth.currentUser?.email.toString()}/image/profile.png")

        refStorage.putFile(fileUri)
            .addOnSuccessListener(
                OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        Log.d("CloudFirestore", "EditProfile: uploadImageToFirebase: ${Firebase.auth.currentUser?.email.toString()}/image/profile.png")
                        vm_user.updateProfile("${Firebase.auth.currentUser?.email.toString()}/image/profile.png", "profileImage", Firebase.auth.currentUser?.email.toString())
                    }
                })
    
            .addOnFailureListener(OnFailureListener { e ->
                print(e.message)
            })
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}