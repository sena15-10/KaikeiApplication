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
import com.example.kaikeiapplication.model.Product
import com.example.kaikeiapplication.model.SalesItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesFragment : Fragment() {

    // 1. サンプルデータは空にしておく（または宣言のみにする）
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter // クラス全体で使えるよ

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
        val tvCart    = view.findViewById<TextView>(R.id.tvCartCount)
        val tvSales   = view.findViewById<TextView>(R.id.tvTotalAmount)
        val btnBuy    = view.findViewById<Button>(R.id.btnPurchase)

        // ── RecyclerViewのセットアップ ──
        adapter = ProductAdapter(productList) {
            updateSummary(tvTotal, tvCart, tvSales)  // 数量変化時に更新
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val database = AppDatabase.getDatabase(requireContext())
        val registrationDao = database.registrationDao()

        // 全商品を取得するクエリを監視 (※Daoに全取得メソッドがない場合は下記参照)
        registrationDao.getAllProducts().observe(viewLifecycleOwner) { productsFromDb ->
            if (productsFromDb != null) {
                productList.clear()
                productList.addAll(productsFromDb)
                adapter.notifyDataSetChanged()
                updateSummary(tvTotal, tvCart, tvSales)
            }
        }


        // ── 購入ボタン ──
        btnBuy.setOnClickListener {
            val cartItems = productList.filter { it.quantity > 0 }

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "カートが空です", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(requireContext())
                    val salesDao = db.salesDao()
                    val registrationDao = db.registrationDao() // 商品マスタ更新用に追加取得

                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    val dateString = dateFormat.format(Date())

                    cartItems.forEach { product ->
                        // 1. 売上履歴を保存
                        val salesItem = SalesItem(
                            productName = product.name,
                            quantity = product.quantity,
                            price = product.price,
                            date = dateString
                        )
                        salesDao.insert(salesItem)

                        // 2. 【重要】在庫を減らし、データベースを更新する
                        product.stock -= product.quantity // メモリ上の値を減らす
                        registrationDao.update(product)   // ★これを追加：DBの値を更新（保存）する
                    }

                    // カートのリセット
                    productList.forEach { it.quantity = 0 }
                    adapter.notifyDataSetChanged()
                    updateSummary(tvTotal, tvCart, tvSales)

                    Toast.makeText(requireContext(), "購入完了しました", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "エラーが発生しました", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── サマリー更新ヘルパー ──
    private fun updateSummary(tvTotal: TextView, tvCart: TextView, tvSales: TextView) {
        val total    = productList.sumOf { it.price.toLong() * it.quantity } // Longに変換してオーバーフロー防止
        val cartQty  = productList.sumOf { it.quantity }

        tvTotal.text  = "¥$total"
        tvCart.text   = "カート内数量：${cartQty}個"
        tvSales.text  = "¥$total"
    }
}
