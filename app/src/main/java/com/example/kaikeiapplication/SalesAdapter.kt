package com.example.kaikeiapplication

import SalesItem
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

    /**
     * 1行分の売上データ表示に必要なビュー（UI部品）を保持するクラスです。
     */
    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName) // 商品名
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)       // 数量
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)             // 単価
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)               // 販売日
    }

    /**
     * 新しい行（ViewHolder）が必要になったときに呼ばれます。
     * item_sales_row.xml を元に1行分のレイアウトを生成します。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_row, parent, false)
        return SalesViewHolder(view)
    }

    /**
     * 指定された位置にあるデータを、ViewHolderの各部品に流し込みます。
     */
    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val item = itemList[position]
        
        // データのセット
        holder.tvProductName.text = item.productName
        holder.tvQuantity.text = "${item.quantity} 個" // 単位を付けてわかりやすく表示
        holder.tvPrice.text = "¥${item.price}"          // 通貨記号を付与
        holder.tvDate.text = item.date
    }

    /**
     * リストに表示するアイテムの総数を返します。
     */
    override fun getItemCount(): Int {
        return itemList.size
    }
}
