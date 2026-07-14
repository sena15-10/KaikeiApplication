package com.example.kaikeiapplication.saleshistory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.R
import com.example.kaikeiapplication.model.SalesItem

/**
 * 販売履歴（売上データ）をリスト表示するためのアダプタークラスです。
 *
 * @param onSelectionModeChanged 選択モードの状態や選択件数が変わるたびにFragmentへ通知するコールバック
 */
class SalesAdapter(
    private var itemList: List<SalesItem>,
    private val onSelectionModeChanged: (isSelectionMode: Boolean, selectedCount: Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    // ── 選択モードの状態 ──────────────────────────
    private var isSelectionMode = false
    private val selectedIds = mutableSetOf<Int>()

    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
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
        holder.tvPrice.text = "¥${item.price * item.quantity}"
        holder.tvDate.text = item.date

        // ── STEP 3: 各アイテムのCheckBoxをVISIBLEに切り替える ──
        // 理由: 選択モード中だけチェックボックスで選択状態を可視化するため
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.cbSelect.isChecked = selectedIds.contains(item.id)
        holder.cbSelect.setOnCheckedChangeListener { _, _ ->
            // ── STEP 5: タップで選択状態をtoggleしてチェックを入れる ──
            toggleSelection(item.id)
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                holder.cbSelect.isChecked = !holder.cbSelect.isChecked
            }
        }

        // ── STEP 1: 長押しを検知してisSelectionModeをtrueにする ──
        // 理由: 長押しをきっかけに複数選択モードへ入れるようにするため
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                enterSelectionMode()
                toggleSelection(item.id)
            }
            true
        }
    }

    override fun getItemCount(): Int = itemList.size

    /**
     * 表示するデータリストを差し替えて再描画する
     * ページが切り替わるたびにこのメソッドを呼ぶ
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SalesItem>) {
        itemList = newList
        notifyDataSetChanged()
    }

    // ── STEP 2: Adapterに選択モードへの切り替えを通知する ──
    // 理由: Fragment側でBottomActionBarなどの表示切り替えを行うため
    @SuppressLint("NotifyDataSetChanged")
    private fun enterSelectionMode() {
        isSelectionMode = true
        notifyDataSetChanged()
        onSelectionModeChanged(true, selectedIds.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionModeChanged(false, 0)
    }

    private fun toggleSelection(id: Int) {
        if (!selectedIds.add(id)) {
            selectedIds.remove(id)
        }

        // 選択件数が0件に戻ったら選択モードを自動的に終了する
        if (selectedIds.isEmpty()) {
            exitSelectionMode()
        } else {
            onSelectionModeChanged(isSelectionMode, selectedIds.size)
        }
    }

    fun getSelectedIds(): Set<Int> = selectedIds.toSet()

    // TODO: [別担当] "選択削除"ボタン押下後のContextMenuによる
    //              削除確認ダイアログを実装する
    //              → registerForContextMenu() / onContextItemSelected() を使用すること
    // TODO: [別担当] 削除実行後のリスト更新とAdapter通知を実装する
}
