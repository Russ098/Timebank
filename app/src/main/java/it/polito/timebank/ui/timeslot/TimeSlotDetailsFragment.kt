package it.polito.timebank.ui.timeslot

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.model.TimeSlotData
import it.polito.timebank.ui.chat.Request
import it.polito.timebank.viewmodels.ChatViewModel
import it.polito.timebank.viewmodels.SkillsViewModel
import it.polito.timebank.viewmodels.TimeSlotViewModel
import kotlin.concurrent.thread

class TimeSlotDetailsFragment : Fragment(R.layout.time_slot_details_fragment) {

    val vm_timeslot by viewModels<TimeSlotViewModel>()
    val vm_skills by viewModels<SkillsViewModel>()
    val vm_chat by viewModels<ChatViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.title = "Advertisement details"
        return inflater.inflate(R.layout.time_slot_details_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getString("id").toString()
        val timeslot: LiveData<TimeSlotData> = vm_timeslot.getTimeSlotById(id)

        val title_details: TextView = view.findViewById(R.id.title_tsdetails)
        val description_details: TextView = view.findViewById(R.id.description_tsdetails)
        val date_details: TextView = view.findViewById(R.id.date_tsdetails)
        val time_details: TextView = view.findViewById(R.id.time_tsdetails)
        val duration_details: TextView = view.findViewById(R.id.duration_tsdetails)
        val location_details: TextView = view.findViewById(R.id.location_tsdetails)
        val other_details: TextView = view.findViewById(R.id.details_tsdetails)
        val skills_details: TextView = view.findViewById(R.id.skills_tsdetails)
        val email_details: TextView = view.findViewById(R.id.email_details)
        activity?.let { it -> timeslot.observe(it){ title_details.text = it.title } }
        activity?.let { it -> timeslot.observe(it){ description_details.text = it.description } }
        activity?.let { it -> timeslot.observe(it){
            val dateFields = it.date!!.split("/")
            if(dateFields.size == 3) {
                val USAdateFormat = dateFields[2] + "/" + dateFields[1] + "/" + dateFields[0]
                date_details.text = USAdateFormat
            }
        } }
        activity?.let { it -> timeslot.observe(it){ time_details.text = it.time } }
        activity?.let { it -> timeslot.observe(it){ duration_details.text = it.duration } }
        activity?.let { it -> timeslot.observe(it){ location_details.text = it.location } }
        activity?.let { it -> timeslot.observe(it){ other_details.text = it.others } }
        activity?.let { it -> timeslot.observe(it){ skills_details.text = it.skill } }
        activity?.let { it -> timeslot.observe(it){
            email_details.text = it.email
            if(email_details.text.toString().equals(Firebase.auth.currentUser?.email.toString())) {
                setHasOptionsMenu(true)
            }else{
                setHasOptionsMenu(false)

                email_details.setTextColor(resources.getColor(R.color.purple_200))
                email_details.setOnClickListener{
                    val bundle = bundleOf("externalUserEmail" to email_details.text)
                    findNavController().navigate(R.id.action_timeSlotDetailsFragment_to_showProfileFragment, bundle)
                }
            }
        } }


        val btnChat: Button = view.findViewById(R.id.btn_chat)

        Log.d("Chat", "email deatil: ${email_details.text.toString()} ")


        activity?.let { it ->
            vm_chat.pendingSend.observe(it) {
                if(email_details.text == Firebase.auth.currentUser?.email.toString()){
                    btnChat.visibility = View.GONE;
                }
                var pending = false
                for (x in it) {
                    if (x.title == title_details.text.toString()) {
                        btnChat.setBackgroundColor(Color.GRAY);
                        btnChat.text = "Pending"
                        btnChat.isClickable = false
                        pending = true
                    }
                }
                if (!pending) {
                    btnChat.setOnClickListener {
                        vm_chat.sendRequest(
                            Firebase.auth.currentUser?.email.toString(),
                            email_details.text.toString(),
                            title_details.text.toString(),
                            false,
                            true
                        )
                    }
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timeslot, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_modify_timeslot -> {
                val id = arguments?.getString("id")
                val bundle = bundleOf("id" to id.toString(), "source" to "edit")
                findNavController().navigate(R.id.action_timeSlotDetailsFragment_to_timeSlotEditFragment, bundle)
                true
            }
            R.id.action_delete_timeslot -> {
                this.view?.let { onAlertDialog(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onAlertDialog(view: View) {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(view.context)

        //set content area
        builder.setMessage("Delete this advertisement?")

        //set positive button
        builder.setPositiveButton("Yes") { _, _ ->
            // User clicked Update Now button
            val id = arguments?.getString("id")
            if (id != null) {

                val currentAdv =  vm_timeslot.getTimeSlotById(id)
                activity?.let { it -> currentAdv.observe(it){
                    val currentAdvSkill = currentAdv.value?.skill?.replace("[", "")
                        ?.replace("]", "")

                    vm_timeslot.deleteAdvertisementById(id)

                    if(currentAdvSkill != null){
                        Log.d("CloudFirestore", "deleteAdvertisementById2: ${currentAdvSkill}")
                        val advList = vm_timeslot.getTimeSlotBySkill(currentAdvSkill,Firebase.auth.currentUser?.email.toString())
                        activity?.let {it -> advList.observe(it){
                            Log.d("CloudFirestore", "deleteAdvertisementById3: ${advList.value}")
                            if(advList.value?.size == 0) {
                                vm_skills.deleteSkill(currentAdvSkill)
                            }
                        }
                        }

                    }
                } }

            }


            findNavController().navigate(R.id.action_timeSlotDetailsFragment_to_timeSlotListFragment)
        }

        //set negative button
        builder.setNegativeButton("No") { dialog, id ->
            // User cancelled the dialog
        }

        builder.show()
    }

}