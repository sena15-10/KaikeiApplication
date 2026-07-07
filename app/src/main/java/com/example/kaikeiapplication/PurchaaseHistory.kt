package com.example.kaikeiapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.database.AppDatabase
import kotlin.math.ceil

/**
 * 販売履歴画面を表示するためのフラグメントクラスです。
 * データベース（Room）から実際の販売履歴を取得し、ページネーション表示します。
 */
class PurchaaseHistory : Fragment() {

    // ── 定数・変数 ──────────────────────────────
    private val PAGE_SIZE = 5
    private var currentPage = 0
    private var salesList: List<SalesItem> = emptyList()

    private lateinit var adapter: SalesAdapter

    // ── ライフサイクル ───────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

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

        // 4. データベースからデータを取得して監視
        val dao = AppDatabase.getDatabase(requireContext()).salesDao()

        // getAllSales() は LiveData<List<SalesItem>> を返すので、データが変わるたびに実行される
        dao.getAllSales().observe(viewLifecycleOwner) { updatedList ->
            salesList = updatedList

            // データ件数が減ってページが範囲外になった場合は補正する
            val totalPages = calcTotalPages()
            if (currentPage > totalPages - 1) {
                currentPage = totalPages - 1
            }

            showPage(tvPageInfo, btnPrev, btnNext)
            updateSalesSummary(view, salesList)
        }
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

    /**
     * データベースから取得したリストに基づいて集計を計算し、UIに反映します。
     */
    private fun updateSalesSummary(rootView: View, salesList: List<SalesItem>) {
        val totalAmount = salesList.sumOf { it.quantity * it.price }
        val totalQuantity = salesList.sumOf { it.quantity }
        val transactionCount = salesList.size

        rootView.findViewById<TextView>(R.id.tvSumSales)?.text = totalAmount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesCount)?.text = transactionCount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesNum)?.text = totalQuantity.toString()
    }
}
