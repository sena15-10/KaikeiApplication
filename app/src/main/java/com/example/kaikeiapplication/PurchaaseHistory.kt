package com.example.kaikeiapplication

import SalesAdapter
import SalesItem
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.Date


class PurchaaseHistory : Fragment() {

    private val salesList = mutableListOf<SalesItem>(
        SalesItem("イチゴクリーム",2,250, "2026-01-01"),
        SalesItem("クッキー＆クリーム",3,250,"2026-01-01"),
        SalesItem("桜餡",1,250,"2026-01-01")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //画面部品の取得

        val rvSalesHistory = view.findViewById<RecyclerView>(R.id.rvSalesHistory)
        val adapter = SalesAdapter(salesList)

        rvSalesHistory.layoutManager = LinearLayoutManager(requireContext())
        rvSalesHistory.adapter = adapter
        setSalesInfo()
    }
    fun setSalesInfo() {
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


    companion object {

    }
}