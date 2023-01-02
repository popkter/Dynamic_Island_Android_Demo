import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customizeview.R
import com.example.customizeview.ShowToast

class CustomizedAdapter(private val dataSet: ArrayList<String>,private val callback: ShowToast) :
    RecyclerView.Adapter<CustomizedAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val content: TextView

        init {
            // Define click listener for the ViewHolder's View.
            title = view.findViewById(R.id.title)
            content = view.findViewById(R.id.content)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recyclerview_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.title.text = (position + 1).toString()
        viewHolder.content.text = dataSet[position]
        viewHolder.itemView.setOnClickListener {
            show(viewHolder.content.text as String)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    private fun show(string: String) {
        callback.show(string)
    }

}
