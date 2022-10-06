package it.polito.timebank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(val data: MutableList<String>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val chatText: TextView = v.findViewById(R.id.skill_item_name)
        private val cardChat: CardView = v.findViewById(R.id.cardView)

        fun bind(adv: String, action:(v:View)->Unit){
            chatText.text = adv
            cardChat.setOnClickListener{
                val bundle = bundleOf(
                    "id" to adv,
                    "filter" to adv)
                it.findNavController().navigate(R.id.action_chatFragment_to_chatDetailsFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.skill_item, parent, false)
        return ChatViewHolder(vg)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(data[position]){}
    }

    override fun getItemCount(): Int = data.size
}


