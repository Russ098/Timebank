package it.polito.timebank.ui.timeslot

import Item
import ItemAdapter
import ItemAdapterFilter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.timebank.MainActivity
import it.polito.timebank.R
import it.polito.timebank.model.TimeSlotData
import it.polito.timebank.viewmodels.TimeSlotViewModel

const val TAG: String = "CloudFirestore"

class TimeSlotListFragment : Fragment(R.layout.time_slot_list_fragment) {

    private val vm_timeslot by viewModels<TimeSlotViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setRvWithTimeSlots()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab : FloatingActionButton? = view.findViewById(R.id.fab)
        val btn_filter: Button = view.findViewById(R.id.filter_btn)
        val btn_order: Button = view.findViewById(R.id.order_btn)

        val filter= arguments?.getString("filter")

        if(!filter.isNullOrEmpty()){
            (activity as MainActivity).supportActionBar?.title = "Advertisements: $filter "
            btn_filter.visibility = View.VISIBLE
            btn_order.visibility = View.VISIBLE
            fab?.visibility = View.INVISIBLE

            vm_timeslot.setTimeSlotBySkill(filter, Firebase.auth.currentUser?.email.toString())

        } else {
            (activity as MainActivity).supportActionBar?.title = "My Advertisements"
            btn_filter.visibility = View.GONE
            btn_order.visibility = View.GONE

            vm_timeslot.getTimeSlotByEmail(Firebase.auth.currentUser?.email.toString())

            fab?.setOnClickListener{
                findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment, bundleOf("source" to "fab"))
            }
        }

        Log.d("CloudFirestore", filter.toString())

        btn_filter.setOnClickListener {
            val popup = PopupMenu(context, btn_filter)
            popup.menuInflater.inflate(R.menu.filter_timeslot, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val itemTitle = item.title.toString()
                val filter = arguments?.getString("filter")
                when (itemTitle) {
                    "All" -> {
                        if (filter != null) {
                            vm_timeslot.setTimeSlotBySkill(filter, Firebase.auth.currentUser?.email.toString())
                        }
                    }
                    "Today" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotOfToday(filter, Firebase.auth.currentUser?.email.toString())
                        }
                    }
                    "Tomorrow" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotOfTomorrow(filter, Firebase.auth.currentUser?.email.toString())
                        }
                    }
                    "This Week" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotOfThisWeek(filter, Firebase.auth.currentUser?.email.toString())
                        }
                    }
                    "This Month" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotOfThisMonth(filter, Firebase.auth.currentUser?.email.toString())
                        }
                    }
                    "Location" -> {
                        val view = layoutInflater.inflate(R.layout.popup_filter_location_menu, null, false)

                        val popupWindow = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
                        popupWindow.isFocusable = true;
                        popupWindow.update();
                        val btnSearchByLocation = view.findViewById<ImageButton>(R.id.btnSearchByLocation)
                        btnSearchByLocation.setOnClickListener{
                            val editTextLocation = view?.findViewById<EditText>(R.id.editTextLocation)
                            Log.d("location", "LOCATION = ${editTextLocation?.text.toString()}" )
                            if (filter != null) {
                                vm_timeslot.getTimeSlotByLocation(editTextLocation?.text.toString(), filter, Firebase.auth.currentUser?.email.toString())
                            }
                            popupWindow.dismiss()
                        }

                    }
                }
                true
            }

            popup.show()
        }

        btn_order.setOnClickListener {
            val popup = PopupMenu(context, btn_order)
            popup.menuInflater.inflate(R.menu.order_timeslot, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val itemTitle = item.title.toString()
                when (itemTitle) {
                    "Date asc" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotDateOrdered(Query.Direction.ASCENDING)
                        }
                    }
                    "Date desc" -> {
                        if (filter != null) {
                            vm_timeslot.getTimeSlotDateOrdered(Query.Direction.DESCENDING)
                        }
                    }
                }
                true
            }

            popup.show()
        }

    }

    private fun setRvWithTimeSlots() {

        activity?.let {
            vm_timeslot.timeSlots.observe(viewLifecycleOwner, Observer { it ->
                val listOfAdvertisement = mutableListOf<Item>()
                for (adv in it) {
                    listOfAdvertisement.add(createAdv(adv));
                }

                val rv = view?.findViewById<RecyclerView>(R.id.rv)
                rv?.layoutManager = LinearLayoutManager(activity)

                val adapter = ItemAdapterFilter(listOfAdvertisement)
                rv?.adapter = adapter

                val emptyList=view?.findViewById<TextView>(R.id.empty_list)
                if (listOfAdvertisement.isNotEmpty()) {
                    emptyList?.visibility = View.GONE
                } else {
                    emptyList?.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun createAdv(timeSlotData: TimeSlotData): Item{
        return Item(
            timeSlotData.id.toString(),
            timeSlotData.title.toString(),
            timeSlotData.description.toString(),
            timeSlotData.date.toString(),
            timeSlotData.time.toString(),
            timeSlotData.duration.toString(),
            timeSlotData.location.toString(),
            timeSlotData.others,
            timeSlotData.email
        )
    }

}