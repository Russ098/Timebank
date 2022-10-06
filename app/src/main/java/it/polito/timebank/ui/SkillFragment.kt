package it.polito.timebank.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.SkillAdapter
import it.polito.timebank.viewmodels.TimeSlotViewModel

class SkillFragment : Fragment() {
    val vm_timeslot by viewModels<TimeSlotViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.skill_item_list, container, false)
        setRvWithTimeSlots()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Services"
    }

    private fun setRvWithTimeSlots() {
        vm_timeslot.getAllTimeSlot()
        activity?.let { it ->
            vm_timeslot.timeSlots.observe(it) {
                val listOfSkills = mutableListOf<String>()

                var cnt = 0
                for (skill in it) {
                    if (!listOfSkills.contains(skill.skill)) {
                        listOfSkills.add(skill.skill)
                        cnt += 1
                    }
                }

                val rv = view?.findViewById<RecyclerView>(R.id.rv)
                rv?.layoutManager = LinearLayoutManager(activity)

                val adapter = SkillAdapter(listOfSkills)
                rv?.adapter = adapter

                val emptySkills = view?.findViewById<TextView>(R.id.empty_skills)
                if (listOfSkills.isNotEmpty()) {
                    emptySkills?.visibility = View.GONE
                } else {
                    emptySkills?.visibility = View.VISIBLE
                }
            }
        }
    }

}