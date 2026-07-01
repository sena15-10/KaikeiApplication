package com.example.kaikeiapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 【商品一覧アダプター】
 * 商品リストをRecyclerViewで表示し、数量の増減操作と合計金額の更新通知を担当します。
 * 
 * @param products 表示対象の商品データリスト（MutableList）
 * @param onQuantityChanged 数量が変更された際に呼び出されるコールバック関数
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    /**
     * 各アイテム（1行分）のUIコンポーネントを保持する内部クラスです。
     * findViewByIdの呼び出し回数を減らし、スクロールのパフォーマンスを向上させます。
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:     TextView = view.findViewById(R.id.tvProductName)
        val tvPrice:    TextView = view.findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val btnMinus:   Button   = view.findViewById(R.id.btnMinus)
        val btnPlus:    Button   = view.findViewById(R.id.btnPlus)
    }

    /**
     * 新しいViewHolderを作成します。
     * ここでレイアウトXML(item_product)を実際のViewオブジェクトに変換します。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    /**
     * 指定された位置(position)のデータをViewに反映させます。
     * ボタンのクリックリスナーなどもここで設定します。
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // 基本データの流し込み
        holder.tvName.text     = product.name
        holder.tvPrice.text    = "¥${product.price} / 個"
        holder.tvQuantity.text = product.quantity.toString()
        holder.tvSubtotal.text = "¥${product.price * product.quantity}"

        // 【プラスボタン】在庫の範囲内で数量を加算し、再描画を通知
        holder.btnPlus.setOnClickListener {
            if (product.quantity < product.stock) {
                product.quantity++
                // この行のデータが変更されたことをRecyclerViewに伝え、再描画させる
                notifyItemChanged(position)
                // 外部（Fragmentなど）に数量変更を通知し、画面全体の合計金額などを再計算させる
                onQuantityChanged()
            }
        }

        // 【マイナスボタン】0を下回らない範囲で数量を減算し、再描画を通知
        holder.btnMinus.setOnClickListener {
            if (product.quantity > 0) {
                product.quantity--
                notifyItemChanged(position)
                onQuantityChanged()
            }
        }
    }

    /**
     * アダプターが管理しているデータの総件数を返します。
     */
    override fun getItemCount() = products.size
}
