package com.example.kaikeiapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kaikeiapplication.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductRegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<FloatingActionButton>(R.id.fadAddProduct)
        addButton?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SaveItemFragment())
        }

        // 商品が1件以上登録されていれば、登録案内画面ではなく商品一覧(ProductList)を表示する
        // 理由: 登録済みなのに毎回この案内画面が出るのを避けるため
        val dao = AppDatabase.getDatabase(requireContext()).registrationDao()
        dao.getAllProducts().observe(viewLifecycleOwner) { products ->
            if (products.isNotEmpty()) {
                (activity as? MainActivity)?.replaceFragment(ProductList())
            }
        }
    }
}
