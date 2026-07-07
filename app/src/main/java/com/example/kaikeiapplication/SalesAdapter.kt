package com.example.kaikeiapplication
import SalesItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


// ① SalesAdapter の始まり（ここから）
class SalesAdapter(private var itemList: List<SalesItem>) :
    RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_row, parent, false)
        return SalesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val item = itemList[position]
        
        holder.tvProductName.text = item.productName
        holder.tvQuantity.text = "${item.quantity}個"
        holder.tvPrice.text = item.price.toString()
        holder.tvDate.text = item.date
    }
    // リストの総数を返す
    override fun getItemCount(): Int {
        return itemList.size
    }
    //うんちっっっっっ
    /**
     * 表示するデータリストを差し替えて再描画する
     * ページが切り替わるたびにこのメソッドを呼ぶ
     */
    fun updateData(newList: List<SalesItem>) {
        itemList = newList          // リストを差し替え
        notifyDataSetChanged()      // RecyclerView全体を再描画
    }


    // ② SalesViewHolder も SalesAdapter の「中」に入れます
        class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }
} // ③ SalesAdapter の終わり（一番最後で閉じる！）
