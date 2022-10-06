package it.polito.timebank

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import it.polito.timebank.databinding.ActivityMainBinding
import it.polito.timebank.model.UserData
import it.polito.timebank.viewmodels.TimeSlotViewModel
import it.polito.timebank.viewmodels.UserViewModel
import java.io.File
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val vm_user by viewModels<UserViewModel>()
    val vm_timeslots by viewModels<TimeSlotViewModel>()
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.timeSlotDetailsFragment, R.id.timeSlotListFragment, R.id.showProfileFragment,
                R.id.skillFragment, R.id.chatFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)



        //////// REALLY DIRTY WAY TO MANAGE THE LOGOUT FROM THE NAVIGATION DRAWER
        //////// DIRTY DIRTY
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.LogOut -> {

                    // handle click
                    Log.d("CloudFirebase", "NEED TO LOG OUT")

                    try{
                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_timeSlotListFragment_to_loginFragment)
                        Firebase.auth.signOut()
                    } catch(e: java.lang.Exception) {
                        try{
                            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_skillFragment_to_loginFragment)
                            Firebase.auth.signOut()
                        } catch(e: java.lang.Exception) {
//                            Log.d("CloudFirestore", "Error in logging out $e")
                            try {
                                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_showProfileFragment_to_loginFragment)
                                Firebase.auth.signOut()
                            } catch (e: java.lang.Exception){

                                try{
                                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_timeSlotDetailsFragment_to_loginFragment)
                                    Firebase.auth.signOut()
                                } catch(e: java.lang.Exception) {
                                    try{
                                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_chatFragment_to_loginFragment)
                                        Firebase.auth.signOut()
                                    } catch(e: java.lang.Exception) {
                                        Log.d("CloudFirestore", "Error in logging out $e")
                                    }
                                }
//                                Log.d("CloudFirestore", "Error in logging out $e")
                            }
                        }
                        Log.d("CloudFirestore", "Error in logging out $e")
                    }

                    Snackbar.make(binding.root, "You successfully logged out from your account",
                        Snackbar.LENGTH_LONG).show()


                    true
                }
            }

            NavigationUI.onNavDestinationSelected(it, navController)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        val name_navHeader = findViewById<TextView>(R.id.name_navHeader)
        val nickname_navHeader = findViewById<TextView>(R.id.nickname_navHeader)
        val profilePic_navHeader = findViewById<ImageView>(R.id.profilePic_navHeader)

        val user: LiveData<UserData> = vm_user.getUserByEmail(Firebase.auth.currentUser?.email.toString())


        this.let { it -> user.observe(it){ name_navHeader.text = it.fullname } }
        this.let { it -> user.observe(it){ nickname_navHeader.text = it.nickname } }

        try{
           // this.let { it -> user.observe(it){ profilePic_navHeader.setImageURI() } }
            this.let { it -> user.observe(it){
                storageReference = FirebaseStorage.getInstance().getReference(it.profileImage)
                Log.d("CloudFirestore", storageReference.toString())
                val localfile: File = File.createTempFile("tempfile", "jpg")
                storageReference.getFile(localfile)
                    .addOnSuccessListener {
                        val bitmap: Bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                        profilePic_navHeader.setImageBitmap(bitmap)
                        Log.d("CloudFirestore", "onSupportNavigateUp -> success in retrieving image")
                    }.addOnFailureListener{
                        Log.e("CloudFirestore", "onSupportNavigateUp -> SOME ERROR OCCURRED")
                    }
            } }
        } catch (e:Exception) {
            Log.d("IMAGE", "Permission denied")
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}