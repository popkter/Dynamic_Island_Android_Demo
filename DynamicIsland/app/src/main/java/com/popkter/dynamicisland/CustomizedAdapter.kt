import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.popkter.dynamicisland.R

class CustomizedAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<CustomizedAdapter.ViewHolder>() {

    companion object {
        const val TAG = "CustomizedAdapter"
    }

    lateinit var detail: IDetail


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val content: TextView

        init {
            title = view.findViewById(R.id.title)
            content = view.findViewById(R.id.content)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recyclerview_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.title.text = (position + 1).toString()
        viewHolder.content.text = dataSet[position]
        viewHolder.itemView.setOnClickListener {
            show(viewHolder.content.text as String)
        }
    }

    override fun getItemCount() = dataSet.size

    private fun show(string: String) {
        detail.show(string)
    }

    interface IDetail {
        fun show(string: String)
    }

}
