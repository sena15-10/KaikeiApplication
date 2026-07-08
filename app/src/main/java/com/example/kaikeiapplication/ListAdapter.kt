package com.example.kaikeiapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.model.Product


class ListAdapter(private var product: List<Product>,
        private val onEditClick: (Product) -> Unit, //編集ボタンを押したとき
        private val onDeleteClick: (Product) -> Unit) : //削除ボタンを押したとき
    RecyclerView.Adapter<ListAdapter.SalesViewHolder>(){

    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listProductName: TextView = itemView.findViewById(R.id.listProductName) // 商品名
        val listQuantity: TextView = itemView.findViewById(R.id.listQuantityS)       // 数量
        val listProductPrice: TextView = itemView.findViewById(R.id.listProductPrice)             // 単価
        val editBtn: Button = itemView.findViewById<Button>(R.id.editBtn)     // 編集ボタン
        val deleteBtn: Button = itemView.findViewById<Button>(R.id.deleteBtn)    //削除ボタン
    }

    /**
     * 新しい行（ViewHolder）が必要になったときに呼ばれます。
     * item_sales_row.xml を元に1行分のレイアウトを生成します。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return SalesViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SalesViewHolder,
        position: Int
    ) {
        val item = product[position]
        //編集ボタンが押されたときに、渡された関数を実行する
        holder.itemView.findViewById<Button>(R.id.editBtn).setOnClickListener {
            onEditClick(item)
        }
        holder.itemView.findViewById<Button>(R.id.deleteBtn).setOnClickListener {
            onDeleteClick(item)
        }
        Log.d("ITEM" , "$item")
        holder.listProductName.text = item.name
        holder.listQuantity.text     = "${item.stock}個"
        holder.listProductPrice.text = "¥${item.price} / 個"
    }



    override fun getItemCount(): Int {
        return product.size
    }

    fun updateList(newProduct: List<Product>){
        Log.d("ITEM" , "${newProduct.size}")
        product = newProduct
        notifyDataSetChanged()
    }
}


