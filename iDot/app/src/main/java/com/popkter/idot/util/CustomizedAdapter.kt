import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.popkter.idot.R


class CustomizedAdapter(
    private val dataSet: ArrayList<String>,
    private val function: (string: String) -> Unit
) :
    RecyclerView.Adapter<CustomizedAdapter.ViewHolder>() {

    companion object {
        const val TAG = "CustomizedAdapter"
    }

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

        /*  val shader: Shader = LinearGradient(
              0f, 0f, 180f, 0f,
              Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP
          )
          viewHolder.content.apply {
              paint.shader = shader
              text = dataSet[position]
          }*/
        viewHolder.itemView.setOnClickListener {
            function(viewHolder.content.text.toString())
        }
    }

    override fun getItemCount() = dataSet.size
}
