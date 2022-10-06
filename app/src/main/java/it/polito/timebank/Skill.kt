package it.polito.timebank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class SkillAdapter(val data: MutableList<String>): RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {
    class SkillViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val skillTV: TextView = v.findViewById(R.id.skill_item_name)
        private val card: CardView = v.findViewById(R.id.cardView)

        fun bind(skill: String, action:(v:View)->Unit){
            skillTV.text = skill
            card.setOnClickListener{
                val bundle = bundleOf("id" to skill,
                    "filter" to skill)
                it.findNavController().navigate(R.id.action_skillFragment_to_timeSlotListFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.skill_item, parent, false)
        return SkillViewHolder(vg)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(data[position]){}
    }

    override fun getItemCount(): Int = data.size
}


