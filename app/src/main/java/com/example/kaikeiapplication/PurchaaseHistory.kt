package com.example.kaikeiapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.database.AppDatabase
import com.example.kaikeiapplication.model.SalesItem

/**
 * 販売履歴画面を表示するためのフラグメントクラスです。
 * データベース（Room）から実際の販売履歴を取得して表示します。
 */
class PurchaaseHistory : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSalesHistory = view.findViewById<RecyclerView>(R.id.rvSalesHistory)
        rvSalesHistory.layoutManager = LinearLayoutManager(requireContext())

        // データベースからデータを取得して監視
        val dao = AppDatabase.getDatabase(requireContext()).salesDao()
        
        // getAllSales() は LiveData<List<SalesItem>> を返すので、データが変わるたびに実行される
        dao.getAllSales().observe(viewLifecycleOwner) { salesList ->
            // 取得したデータをアダプターにセット
            val adapter = SalesAdapter(salesList)
            rvSalesHistory.adapter = adapter

            // 画面上部の集計情報を更新
            updateSalesSummary(view, salesList)
        }
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
