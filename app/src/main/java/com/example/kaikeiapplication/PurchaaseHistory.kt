package com.example.kaikeiapplication

import SalesItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class PurchaaseHistory : Fragment() {

    // ── 定数・変数 ──────────────────────────────
    private val PAGE_SIZE = 5
    private var currentPage = 0

    // テストデータ
    private val salesList = mutableListOf<SalesItem>(
        SalesItem("サンドイッチ1", 2, 500, "2026-01-01"),
        SalesItem("サンドイッチ1", 4, 500, "2026-01-01"),
        SalesItem("サンドイッチ1", 4, 500, "2026-01-01"),
        SalesItem("サンドイッチ1", 4, 500, "2026-01-01"),
        SalesItem("サンドイッチ1", 4, 500, "2026-01-01"),
        SalesItem("サンドイッチ1", 4, 500, "2026-01-01"),
        SalesItem("サンドイッチ2", 1, 400, "2026-01-02"),
        SalesItem("サンドイッチ2", 3, 400, "2026-01-02"),
        SalesItem("サンドイッチ3", 2, 350, "2026-01-03"),
        SalesItem("サンドイッチ3", 5, 350, "2026-01-03"),
        SalesItem("サンドイッチ3", 1, 350, "2026-01-03"),
    )

    private lateinit var adapter: SalesAdapter

    // ── ライフサイクル ───────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

    // ★重要：Viewに関する処理はすべてここに移します
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Viewの取得
        val rvSalesHistory = view.findViewById<RecyclerView>(R.id.rvSalesHistory)
        val btnPrev = view.findViewById<Button>(R.id.btnPrev)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val tvPageInfo = view.findViewById<TextView>(R.id.tvPageInfo)

        // 2. RecyclerView の初期セットアップ
        adapter = SalesAdapter(emptyList())
        rvSalesHistory.layoutManager = LinearLayoutManager(requireContext())
        rvSalesHistory.adapter = adapter

        // 3. リスナーの設定
        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                showPage(tvPageInfo, btnPrev, btnNext)
            }
        }

        btnNext.setOnClickListener {
            val totalPages = calcTotalPages()
            if (currentPage < totalPages - 1) {
                currentPage++
                showPage(tvPageInfo, btnPrev, btnNext)
            }
        }

        // 4. 初期表示
        showPage(tvPageInfo, btnPrev, btnNext)
        updateSalesSummary(view)
    }

    // ── ヘルパーメソッド ─────────────────────────

    private fun showPage(tvPageInfo: TextView, btnPrev: Button, btnNext: Button) {
        val totalPages = calcTotalPages()

        // 表示範囲の計算
        val fromIndex = currentPage * PAGE_SIZE
        val toIndex = minOf(fromIndex + PAGE_SIZE, salesList.size)

        // リストが空でない場合のみsubListを取得
        val currentDisplayList = if (salesList.isNotEmpty()) {
            salesList.subList(fromIndex, toIndex)
        } else {
            emptyList()
        }

        // Adapterの更新
        adapter.updateData(currentDisplayList)

        // UI更新
        tvPageInfo.text = "${currentPage + 1} / $totalPages ページ"
        btnPrev.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < totalPages - 1
    }

    private fun calcTotalPages(): Int {
        if (salesList.isEmpty()) return 1
        return ceil(salesList.size.toDouble() / PAGE_SIZE).toInt()
    }

    private fun updateSalesSummary(rootView: View) {
        val totalAmount = salesList.sumOf { it.quantity * it.price }
        val totalQuantity = salesList.sumOf { it.quantity }
        val transactionCount = salesList.size

        rootView.findViewById<TextView>(R.id.tvSumSales)?.text = totalAmount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesCount)?.text = transactionCount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesNum)?.text = totalQuantity.toString()
    }
}