package it.polito.timebank.ui.timeslot

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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

class ChatDetailsFragment : Fragment(R.layout.time_slot_details_fragment) {

    val vm_chat by viewModels<ChatViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.title = "Chat details"
        return inflater.inflate(R.layout.fragment_chat_details, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getString("id").toString()
        Log.d("Chat", "valore id: $id")

        //TODO ottenere il timeslot tramite title e email
    }

}