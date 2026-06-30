package com.example.kaikeiapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SalesFragment : Fragment() {

    // ── サンプルデータ（後でSharedPreferencesから読む想定）──
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
        recycler.layoutManager = LinearLayoutManager(requireContext()) //縦方向に配置
        recycler.adapter = adapter

        // ── 初期表示の更新 ──
        updateSummary(tvTotal, tvStock, tvCart, tvSales)

        // ── 購入ボタン ──
        btnBuy.setOnClickListener {
            val total = productList.sumOf { it.price * it.quantity }
            Toast.makeText(requireContext(), "購入しました：¥$total", Toast.LENGTH_SHORT).show()
            // ここに購入後の処理（数量リセット・履歴保存等）を追加する
        }
    }

    // ── サマリー更新ヘルパー ──
    private fun updateSummary(
        tvTotal: TextView, tvStock: TextView,
        tvCart: TextView, tvSales: TextView
    ) {
        val total    = productList.sumOf { it.price * it.quantity }
        val cartQty  = productList.sumOf { it.quantity }
        val minStock = productList.minOfOrNull { it.stock } ?: 0

        tvTotal.text  = "¥$total"
        tvCart.text   = "カート内数量：${cartQty}個"
        tvStock.text  = minStock.toString()
        tvSales.text  = "¥$total"
    }
}
