package com.example.kaikeiapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.database.AppDatabase
import com.example.kaikeiapplication.model.SalesItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesFragment : Fragment() {

    // ── サンプルデータ（後で設定から取得する想定）──
    private val productList = mutableListOf(
        Product(1, "サンド1", 500, 0, 50),
        Product(2, "サンド2", 400, 0, 50),
        Product(3, "サンド3", 350, 0, 50),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Viewの取得 ──
        val recycler  = view.findViewById<RecyclerView>(R.id.recyclerProducts)
        val tvTotal   = view.findViewById<TextView>(R.id.tvGrandTotal)
        val tvStock   = view.findViewById<TextView>(R.id.tvStockCount)
        val tvCart    = view.findViewById<TextView>(R.id.tvCartCount)
        val tvSales   = view.findViewById<TextView>(R.id.tvTotalAmount)
        val btnBuy    = view.findViewById<Button>(R.id.btnPurchase)

        // ── RecyclerViewのセットアップ ──
        val adapter = ProductAdapter(productList) {
            updateSummary(tvTotal, tvStock, tvCart, tvSales)  // 数量変化時に更新
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // ── 初期表示の更新 ──
        updateSummary(tvTotal, tvStock, tvCart, tvSales)

        // ── 購入ボタン ──
        btnBuy.setOnClickListener {
            // 1. カートに入っている商品（数量 > 0）を抽出
            val cartItems = productList.filter { it.quantity > 0 }
            
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "カートが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val total = cartItems.sumOf { it.price * it.quantity }
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val dateString = dateFormat.format(Date())

            // 2. データベースへの保存処理 (非同期)
            lifecycleScope.launch {
                val dao = AppDatabase.getDatabase(requireContext()).salesDao()
                
                cartItems.forEach { product ->
                    val salesItem = SalesItem(
                        productName = product.name,
                        quantity = product.quantity,
                        price = product.price,
                        date = dateString
                    )
                    // コメントアウト：ここでDBに1件ずつ保存
                    dao.insert(salesItem)
                }

                // 3. 処理後のクリーンアップ
                // コメントアウト：カート内数量をすべて0にリセット
                productList.forEach { it.quantity = 0 }
                
                // 表示を更新
                adapter.notifyDataSetChanged()
                updateSummary(tvTotal, tvStock, tvCart, tvSales)

                Toast.makeText(requireContext(), "購入データを保存しました：¥$total", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── サマリー更新ヘルパー ──
    private fun updateSummary(
        tvTotal: TextView, tvStock: TextView,
        tvCart: TextView, tvSales: TextView
    ) {
        val total    = productList.sumOf { it.price * it.quantity }
        val cartQty  = productList.sumOf { it.quantity }
        val minStock = productList.minOfOrNull { it.stock - it.quantity } ?: 0

        tvTotal.text  = "¥$total"
        tvCart.text   = "カート内数量：${cartQty}個"
        tvStock.text  = minStock.toString()
        tvSales.text  = "¥$total"
    }
}
