package com.example.kaikeiapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 販売履歴（売上データ）をリスト表示するためのアダプタークラスです。
 */
class SalesAdapter(private val itemList: List<SalesItem>) :
    RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_row, parent, false)
        return SalesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val item = itemList[position]
        
        holder.tvProductName.text = item.productName
        holder.tvQuantity.text = "${item.quantity} 個"
        holder.tvPrice.text = "¥${item.price}"
        holder.tvDate.text = item.date
    }

    override fun getItemCount(): Int = itemList.size
}
