import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebank.R

data class Item(val id: String,
                val title:String,
                val description:String,
                val date_time:String,
                val time:String,
                val duration:String,
                val location:String,
                val others:String,
                val email: String)

class ItemAdapter(val data:MutableList<Item>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.title_item)
        private val datetime: TextView = v.findViewById(R.id.date_time_item)
        private val edit: ImageButton = v.findViewById(R.id.edit)
        private val card: CardView = v.findViewById(R.id.cardView)

        fun bind(user:Item, action:(v:View)->Unit){
            title.text = user.title

            val dateValues = user.date_time.split("/")
            val USAformat = dateValues[2]+"/"+dateValues[1]+"/"+dateValues[0]
            datetime.text = USAformat
            edit.setOnClickListener{
                val bundle = bundleOf("id" to user.id.toString(),
                                            "title" to user.title,
                                            "date" to user.date_time,
                                            "source" to "edit")
                Log.d("ITEM_INFO", user.id.toString() + user.title + user.date_time)
                it.findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment, bundle)
            }
            card.setOnClickListener{
                val bundle = bundleOf("id" to user.id.toString(),
                                            "title" to user.title,
                                            "description" to user.description,
                                            "date" to user.date_time,
                                            "time" to user.time,
                                            "duration" to user.duration,
                                            "location" to user.location,
                                            "others" to user.others)
                Log.d("ITEM_INFO", user.id.toString() + user.title + user.date_time)
                it.findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment, bundle)
            }
        }
        fun unbind(){
            edit.setOnClickListener(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(vg)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(data[position]){}
    }

    override fun getItemCount(): Int = data.size
}

class ItemAdapterFilter(val data:MutableList<Item>): RecyclerView.Adapter<ItemAdapterFilter.ItemViewHolder>() {
    class ItemViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.title_item)
        private val datetime: TextView = v.findViewById(R.id.date_time_item)
        private val card: CardView = v.findViewById(R.id.cardView)
        private val edit: ImageButton = v.findViewById(R.id.edit)

        fun bind(user:Item, action:(v:View)->Unit){
            title.text = user.title

            val dateValues = user.date_time.split("/")
            val USAformat = dateValues[2]+"/"+dateValues[1]+"/"+dateValues[0]
            datetime.text = USAformat
            edit.visibility = View.GONE
            card.setOnClickListener{
                val bundle = bundleOf("id" to user.id.toString(),
                    "title" to user.title,
                    "description" to user.description,
                    "date" to user.date_time,
                    "time" to user.time,
                    "duration" to user.duration,
                    "location" to user.location,
                    "others" to user.others)
                Log.d("ITEM_INFO", user.id.toString() + user.title + user.date_time)
                it.findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(vg)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(data[position]){}
    }

    override fun getItemCount(): Int = data.size
}



