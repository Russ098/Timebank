package it.polito.timebank.ui.chat
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebank.ChatAdapter
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.SkillAdapter
import it.polito.timebank.viewmodels.ChatViewModel
import it.polito.timebank.viewmodels.TimeSlotViewModel

class ChatFragment : Fragment() {

    private val vm_chat by viewModels<ChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        setRvWithChats()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Chat"
    }

    private fun setRvWithChats() {
        activity?.let { it ->
            vm_chat.pendingReceived.observe(it) {
                val listOfChat = mutableListOf<String>()

                for (chat in it) {
                    listOfChat.add(chat.title)
                }

                val rv = view?.findViewById<RecyclerView>(R.id.rv_chat)
                rv?.layoutManager = LinearLayoutManager(activity)

                val adapter = ChatAdapter(listOfChat)
                rv?.adapter = adapter

                val emptyChat = view?.findViewById<TextView>(R.id.empty_chat)
                if (listOfChat.isNotEmpty()) {
                    emptyChat?.visibility = View.GONE
                } else {
                    emptyChat?.visibility = View.VISIBLE
                }
            }
        }
    }
}