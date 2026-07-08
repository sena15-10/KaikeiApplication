package com.example.kaikeiapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope // ここに移動
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch // ここに移動

class ProductList : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        adapter = ListAdapter(
            product = emptyList(),
            onEditClick = { product ->
                val bundle = Bundle().apply {
                    putInt("productId", product.id)
                }
                val fragment = SaveItemFragment().apply {
                    arguments = bundle
                }
                (activity as? MainActivity)?.replaceFragment(fragment)
            },
            onDeleteClick = { product ->
                // ダイアログを作成
                val dialogFragment = DeleteDialogFragment()

                // 「はい」が押された時の処理をセット
                dialogFragment.onConfirmDelete = {
                    // コルーチンでデータベースから削除を実行
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.registrationDao().delete(product)
                    }
                }

                // ダイアログを表示
                dialogFragment.show(parentFragmentManager, "DeleteDialogFragment")
            }
        )

        val addButton = view.findViewById<FloatingActionButton>(R.id.fadAddProduct2)
        addButton?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SaveItemFragment())
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.registerProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        db.registrationDao().getAllProducts().observe(viewLifecycleOwner) { products ->
            Log.d("ProductList", "取得件数：${products.size}件")
            adapter.updateList(products)
        }
    }
}
