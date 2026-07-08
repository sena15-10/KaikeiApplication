package com.example.kaikeiapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.model.Product

/**
 * 【商品一覧アダプター】
 * 商品リストを表示し、数量の増減操作を担当します。
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:     TextView = view.findViewById(R.id.listProductName)
        val tvPrice:    TextView = view.findViewById(R.id.listProductPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvSubtotal: TextView = view.findViewById(R.id.listSubtotal)
        val tvStockCount: TextView = view.findViewById(R.id.tvStockCount)
        val btnMinus:   Button   = view.findViewById(R.id.btnMinus)
        val btnPlus:    Button   = view.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.tvName.text     = product.name
        holder.tvPrice.text    = "¥${product.price} / 個"
        holder.tvStockCount.text = product.stock.toString()
        holder.tvQuantity.text = product.quantity.toString()
        holder.tvSubtotal.text = "¥${product.price * product.quantity}"

        // ---------------------------------------------------------
        // 【プラスボタン：加算処理】
        // ---------------------------------------------------------



        holder.btnPlus.setOnClickListener {
            // 在庫数（product.stock）を超えない範囲で、商品の数量を1増やす
            if (product.quantity < product.stock + product.quantity) {
                product.quantity++
                product.stock--
                
                // 特定の行の表示（数量と小計）を更新するためにアダプターへ通知
                notifyItemChanged(position)
                
                // Fragment側の合計金額表示などを更新するためのコールバックを実行
                onQuantityChanged()
            }
        }

        // ---------------------------------------------------------
        // 【マイナスボタン：減算処理】
        // ---------------------------------------------------------
        holder.btnMinus.setOnClickListener {
            // 数量が0より大きい場合のみ、数量を1減らす（マイナスにはしない）
            if (product.quantity > 0) {
                product.quantity--
                product.stock++
                
                // 表示を更新するためにアダプターへ通知
                notifyItemChanged(position)
                
                // Fragment側の合計金額表示などを更新
                onQuantityChanged()
            }
        }
    }
//
    override fun getItemCount() = products.size
}
