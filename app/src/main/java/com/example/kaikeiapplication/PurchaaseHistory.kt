package com.example.kaikeiapplication


import com.example.kaikeiapplication.SalesItem
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 販売履歴画面を表示するためのフラグメントクラスです。
 */
class PurchaaseHistory : Fragment() {

    // 表示用のテスト用データリスト
    private val salesList = mutableListOf<SalesItem>(
        SalesItem(1,"イチゴクリーム", 2, 250, "2026-01-01"),
        SalesItem(2,"クッキー＆クリーム", 3, 250, "2026-01-01"),
        SalesItem(3,"桜餡", 1, 250, "2026-01-01")
    )

    // フラグメントのレイアウトを作成し、ビューを返します
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_purchaase_history レイアウトをインフレート（生成）します
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

    // ビューが作成された直後に呼び出される初期化処理です
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView（履歴リスト）の取得とアダプターの紐付け
        val rvSalesHistory = view.findViewById<RecyclerView>(R.id.rvSalesHistory)
        val adapter = SalesAdapter(salesList)

        // レイアウトマネージャーの設定（縦方向のリスト表示）
        rvSalesHistory.layoutManager = LinearLayoutManager(requireContext())
        rvSalesHistory.adapter = adapter
        setSalesInfo()
    }
    fun setSalesInfo() {
        val tvNothing = view?.findViewById<TextView>(R.id.tvNothing)
        val layoutNothing = view?.findViewById<View>(R.id.nothingImg)
        if (salesList.isEmpty()){
            tvNothing?.visibility = View.VISIBLE
            layoutNothing?.visibility = View.VISIBLE
        }else{
            var sum = 0
            var salesNum = 0
            val salesCount = salesList.size

            for (item in salesList) {
                sum += item.quantity * item.price
                salesNum += item.quantity
            }
            val tvSumSales = view?.findViewById<TextView>(R.id.tvSumSales)
            val tvSalesCount = view?.findViewById<TextView>(R.id.tvSalesCount)
            val tvSalesNum = view?.findViewById<TextView>(R.id.tvSalesNum)
            tvSumSales?.text = sum.toString()
            tvSalesCount?.text = salesCount.toString()
            tvSalesNum?.text = salesNum.toString()
            }
    }

    /**
     * 販売リストに基づいて集計（合計金額、販売数、取引数）を計算し、UIに反映します。
     * @param rootView 検索対象となる親ビュー
     */
    private fun updateSalesSummary(rootView: View) {
        // 合計金額の算出 (数量 × 単価 の合計)
        val totalAmount = salesList.sumOf { it.quantity * it.price }
        // 総販売個数の算出
        val totalQuantity = salesList.sumOf { it.quantity }
        // 取引件数 (リストの要素数)
        val transactionCount = salesList.size

        // 各TextViewに計算結果をセット
        rootView.findViewById<TextView>(R.id.tvSumSales)?.text = totalAmount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesCount)?.text = transactionCount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesNum)?.text = totalQuantity.toString()
    }
}
