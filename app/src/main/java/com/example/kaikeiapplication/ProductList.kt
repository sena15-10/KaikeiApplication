package com.example.kaikeiapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton



class ProductList : Fragment() {

    private lateinit var adapter: ListAdapter
    private lateinit var db: AppDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //データベースのインスタンスを取得する。
        db = AppDatabase.getDatabase(requireContext())
//        アダプターの初期化
        adapter = ListAdapter(
            product = emptyList(),
            //編集ボタンの処理
            onEditClick = { product ->
                Log.d("編集ボタンがクリックされた", "商品ID: ${product.id}")
                val bundle = Bundle().apply {
                    putInt("productId", product.id)
                }
                //遷移先のfragmentを作成して引数をセットする
                val fragment = SaveItemFragment().apply {
                    arguments = bundle
                }
                //画面遷移を実行(既存のreplaceFragmentを利用)
                (activity as? MainActivity)?.replaceFragment(fragment)
            }
        )


        val addButton = view.findViewById<FloatingActionButton>(R.id.fadAddProduct2)
        addButton?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SaveItemFragment())
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.registerProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //LinearLayoutは縦方向に１列ずつ並べる設定
        recyclerView.adapter = adapter
        //LiveDataを監視して、「データが変わったら自動でこの処理よんでねという登録」
        db.registrationDao().getAllProducts().observe(viewLifecycleOwner) { products ->
            Log.d("ProductList", "取得件数：${products.size}件")
            adapter.updateList(products)
        }

    }
}
