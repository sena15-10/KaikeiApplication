import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.R

// ① SalesAdapter の始まり（ここから）
class SalesAdapter(private val itemList: List<SalesItem>) :
    RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {
    // 1行分のレイアウト（XML）を読み込む
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_row, parent, false)
        return SalesViewHolder(view)
    }
    // 指定された位置（position）のデータを、Viewにセットする
    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvProductName.text = item.productName
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvPrice.text = item.price.toString()
        holder.tvDate.text = item.date
    }
    // リストの総数を返す
    override fun getItemCount(): Int {
        return itemList.size
    }
    // ② SalesViewHolder も SalesAdapter の「中」に入れます
        class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }
} // ③ SalesAdapter の終わり（一番最後で閉じる！）