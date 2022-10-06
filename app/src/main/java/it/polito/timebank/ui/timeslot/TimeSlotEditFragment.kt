package it.polito.timebank.ui.timeslot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.model.TimeSlotData
import it.polito.timebank.model.UserData
import it.polito.timebank.viewmodels.SkillsViewModel
import it.polito.timebank.viewmodels.TimeSlotViewModel
import it.polito.timebank.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class TimeSlotEditFragment : Fragment(R.layout.time_slot_edit_fragment) {

    val vm_timeslot by viewModels<TimeSlotViewModel>()
    val vm_user by viewModels<UserViewModel>()
    val vm_skills by viewModels<SkillsViewModel>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getString("id")

        val addButton : Button? = view.findViewById(R.id.addItemButton)

        val edit_title = view.findViewById<EditText>(R.id.title_tsedit)
        val edit_description = view.findViewById<EditText>(R.id.description_tsedit)
        val edit_date = view.findViewById<EditText>(R.id.editDate)
        val edit_time = view.findViewById<EditText>(R.id.editTime)
        val edit_duration = view.findViewById<EditText>(R.id.duration_tsedit)
        val edit_location = view.findViewById<EditText>(R.id.location_tsedit)
        val edit_details = view.findViewById<EditText>(R.id.details_tsedit)
        val edit_skills = view.findViewById<EditText>(R.id.skillsRequired_edit)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)+1

        edit_date.setOnClickListener {
            val dpd =
                activity?.let {
                    DatePickerDialog(it, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        // Display Selected date in textbox
                        edit_date.setText("" + dayOfMonth + "/" + (monthOfYear+1) + "/" + year)

                    }, year, month, day)
                }

            if (dpd != null) {
                dpd.show()
            }
        }

        var mTimePicker: TimePickerDialog
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        edit_time.setOnClickListener{
            mTimePicker = TimePickerDialog(this.context, object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    edit_time.setText(String.format("%02d:%02d", hourOfDay, minute))
                }
            }, hour, minute, false)

            mTimePicker.show()
        }

        if (arguments != null && addButton != null) {
            (activity as MainActivity).supportActionBar?.title = "Create an Advertisement"

            Log.d("FABVISIBILITY", arguments?.get("source").toString())

            if(arguments?.getString("source").toString() == "fab"){
                addButton.visibility = View.VISIBLE
                addButton.setOnClickListener {

                    hideKeyboard()

                    if(checkFields(edit_title, edit_date, edit_time, edit_duration, edit_location, edit_skills)){

                        val splittedDate = edit_date.text.toString().split("/")
                        val dateUSAFormat = splittedDate[2]+"/"+splittedDate[1]+"/"+splittedDate[0]

                        vm_timeslot.addAdv(edit_title.text.toString(),
                                edit_description.text.toString(),
                                dateUSAFormat,
                                edit_time.text.toString(),
                                edit_duration.text.toString(),
                                edit_location.text.toString(),
                                edit_details.text.toString(),
                                Firebase.auth.currentUser?.email.toString(),
                                edit_skills.text.toString(),
                                true)

                        vm_skills.addSkill(edit_skills?.text.toString())

                        var title_for_snackbar = edit_title.text.toString()
                        Snackbar.make(view, "Successfully added $title_for_snackbar to the activities", Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }

                }
            } else {
                (activity as MainActivity).supportActionBar?.title = "Edit the Advertisement"
                addButton.text = "Save"

                val id = arguments?.getString("id")
                val timeslot: LiveData<TimeSlotData> = vm_timeslot.getTimeSlotById(id.toString())

                addButton.setOnClickListener {

                    hideKeyboard()

                    if ( checkFieldsOnSave(edit_date, edit_time, edit_duration)){

                        if (edit_title?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_title?.text.toString(), "title", id.toString())

                        if (edit_description?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_description?.text.toString(), "description", id.toString())

                        if (edit_date?.text.toString().isNotEmpty()){
                            val splittedDate = edit_date.text.toString().split("/")
                            val dateUSAFormat = splittedDate[2]+"/"+splittedDate[1]+"/"+splittedDate[0]

                            vm_timeslot.updateTimeSlot(dateUSAFormat, "date", id.toString())
                        }


                        if (edit_time?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_time?.text.toString(), "time", id.toString())

                        if (edit_duration?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_duration?.text.toString(), "duration", id.toString())

                        if (edit_location?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_location?.text.toString(), "location", id.toString())

                        if (edit_location?.text.toString().isNotEmpty())
                            vm_timeslot.updateTimeSlot(edit_location?.text.toString(), "location", id.toString())

                        if (edit_skills?.text.toString().isNotEmpty()){
                            vm_timeslot.updateTimeSlot(edit_skills?.text.toString(), "skills", id.toString())
                            vm_skills.addSkill(edit_skills?.text.toString())
                        }


                        findNavController().popBackStack()

                    }
                }

                activity?.let { it -> timeslot.observe(it){ edit_title.hint = it.title } }
                activity?.let { it -> timeslot.observe(it){ edit_description.hint = it.description } }
                activity?.let { it -> timeslot.observe(it){
                    val dateFields = it.date!!.split("/")
                    val USAdateFormat = dateFields[2]+"/"+dateFields[1]+"/"+dateFields[0]
                    edit_date.hint = USAdateFormat
                } }
                activity?.let { it -> timeslot.observe(it){ edit_time.hint = it.time } }
                activity?.let { it -> timeslot.observe(it){ edit_duration.hint = it.duration } }
                activity?.let { it -> timeslot.observe(it){ edit_location.hint = it.location } }
                activity?.let { it -> timeslot.observe(it){ edit_details.hint = it.others } }
                activity?.let { it -> timeslot.observe(it){ edit_skills.hint = it.skill } }

            }
        } else {
            addButton?.visibility = View.INVISIBLE
        }

    }

    fun checkFields(edit_title: EditText, edit_date: EditText,
                    edit_time: EditText, edit_duration: EditText,
                    edit_location: EditText, edit_skills: EditText): Boolean {

        if(edit_title.text.toString().isNotEmpty()){
            edit_title.error = null
        } else {
            edit_title.error = "Empty title"
        }

        if (!checkDateValidity(edit_date.text.toString())) {
            edit_date.error = "Invalid Date"
        } else {
            edit_date.error = null
        }

        if(edit_location.text.toString().isNotEmpty()){
            edit_location.error = null
        } else {
            edit_location.error = "Empty Location"
        }
        if(checkTime(edit_time)) {
            edit_time.error = null
        } else {
            edit_time.error = "Invalid hh:mm"
        }

        if(edit_duration.text.toString().isDigitsOnly() and edit_duration.text.toString().isNotEmpty()){
            edit_duration.error = null
        } else {
            edit_duration.error = "Empty Duration"
        }

        if(edit_skills.text.toString().isNotEmpty()){
            // TO CHECK THAT AN ADV HAS THE SKILLS OF THE USER
//            val skills = vm_user.getSkillsByEmail(Firebase.auth.currentUser?.email.toString())
//            Log.d("CloudFirestore", "TSEditFrag: checkFields: ${skills}")
            edit_skills.error = null
        } else {
            edit_skills.error = "Empty Skills"
        }

        if(edit_skills.text.toString().split(",").size==1){
            // TO CHECK THAT AN ADV HAS THE SKILLS OF THE USER
//            val skills = vm_user.getSkillsByEmail(Firebase.auth.currentUser?.email.toString())
//            Log.d("CloudFirestore", "TSEditFrag: checkFields: ${skills}")
            edit_skills.error = null
        } else {
            edit_skills.error = "Just One Skill"
        }

        return (edit_title.text.toString().isNotEmpty() and
                checkDateValidity(edit_date.text.toString()) and
                edit_location.text.toString().isNotEmpty() and
                edit_skills.text.toString().isNotEmpty() and
                (edit_skills.text.toString().split(",").size==1) and
                checkTime(edit_time) and
                edit_duration.text.toString().isDigitsOnly() and
                edit_duration.text.toString().isNotEmpty());
    }

    fun checkFieldsOnSave(edit_date: EditText?, edit_time: EditText?, edit_duration: EditText?): Boolean{

        if( edit_date?.text.toString().isNotEmpty()){
            if (checkDateValidity(edit_date?.text.toString())) {
                edit_date?.error = null
            } else {
                edit_date?.error = "Invalid Date"

            }
        }

        if(edit_time?.text.toString().isNotEmpty()){
            if(checkTime(edit_time)) {
                edit_time?.error = null
            } else {
                edit_time?.error = "Invalid hh:mm"
            }
        }


        if(edit_duration?.text.toString().isNotEmpty()){
            if(edit_duration?.text.toString().isDigitsOnly() and edit_duration?.text.toString().isNotEmpty()){
                edit_duration?.error = null
            }else{
                edit_duration?.error = "Invalid duration"
            }
        }

        if(!edit_time?.error.isNullOrEmpty() or !edit_date?.error.isNullOrEmpty() or !edit_duration?.error.isNullOrEmpty()) return false

        return true

    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun checkTime(edit_time: EditText?): Boolean{
        val flag = edit_time?.text.toString().replace(" ","").matches("^([0-1]?[0-9]|2[0-3]):[0-5]?[0-9]\$".toRegex())
        if (flag) {
            val hour = edit_time?.text.toString().split(":")[0].toInt()
            val minute = edit_time?.text.toString().split(":")[1].toInt()
            edit_time?.setText(String.format("%02d:%02d", hour, minute))
            return true
        }
        else {
            return false
        }
    }


    fun checkDateValidity(date: String): Boolean{

        var date = date

        // Check if date is empty
        if(date.isNotEmpty()){

            if(date.matches("^([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})$".toRegex())){
                date = date.replace('-','/')
                date = date.replace('.','/')

                val todayDate: Date = Date()
                val formatter = SimpleDateFormat("dd/MM/yyyy")
                val dateUser = formatter.parse(date)

                if (todayDate.after(dateUser)){
                    return false
                }
                return true
            } else {
                return false
            }
        } else {   // if empty date -> INVALID DATE
            return false
        }
    }

}
