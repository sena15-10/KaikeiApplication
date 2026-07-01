package com.example.kaikeiapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val products: MutableList<Product>,
    private val onQuantityChanged: () -> Unit  // 数量変化を通知するコールバック
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // ── ViewHolder：1行分のViewをまとめて持つ ──────────
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:     TextView = view.findViewById(R.id.tvProductName)
        val tvPrice:    TextView = view.findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val btnMinus:   Button   = view.findViewById(R.id.btnMinus)
        val btnPlus:    Button   = view.findViewById(R.id.btnPlus)
    }

    // ── 行のレイアウト（item_product.xml）をふくらませる ──
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    // ── 各行にデータをセットする ──────────────────────
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.tvName.text     = product.name
        holder.tvPrice.text    = "¥${product.price} / 個"
        holder.tvQuantity.text = product.quantity.toString()
        holder.tvSubtotal.text = "¥${product.price * product.quantity}"

        // ＋ボタン：在庫が残っている場合のみ数量を増やす
        holder.btnPlus.setOnClickListener {
            if (product.quantity < product.stock) {
                product.quantity++
                notifyItemChanged(position)  // この行だけ再描画
                onQuantityChanged()          // SalesFragmentへ通知
            }
        }

        // － ボタン：0以上のときだけ数量を減らす
        holder.btnMinus.setOnClickListener {
            if (product.quantity > 0) {
                product.quantity--
                notifyItemChanged(position)
                onQuantityChanged()
            }
        }
    }

    override fun getItemCount() = products.size
}
