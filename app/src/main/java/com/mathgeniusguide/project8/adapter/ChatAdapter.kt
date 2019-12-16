import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChatItem
import kotlinx.android.synthetic.main.chat_item.view.*

class ChatAdapter (private val items: List<ChatItem>, val context: Context, val userkey: String, val yourPhoto: String?, val theirPhoto: String?) : RecyclerView.Adapter<ChatAdapter.ViewHolder> () {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = items[position]
        Glide.with(context).load(yourPhoto).into(holder.yourImage)
        holder.yourImage.visibility = if (i.from == userkey) View.VISIBLE else View.GONE
        Glide.with(context).load(theirPhoto).into(holder.theirImage)
        holder.theirImage.visibility = if (i.to == userkey) View.VISIBLE else View.GONE
        holder.chatText.gravity = if (i.from == userkey) Gravity.END else Gravity.START
        holder.chatText.text = i.text
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val yourImage = view.yourImage
        val theirImage = view.theirImage
        val chatText = view.chatText
    }
}