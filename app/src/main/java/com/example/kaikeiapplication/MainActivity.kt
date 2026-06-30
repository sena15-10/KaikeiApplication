package com.example.kaikeiapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
//            aaaa
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if (savedInstanceState == null) {
            replaceFragment(SalesFragment())//このコードが画面遷移用のコード
            supportFragmentManager.beginTransaction()
//                .replace(R.id.main, SalesFragment()) 　//2行目のコードを書いてしまうと画面遷移がうまくいかない
                .commit()
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_sales -> {
                    replaceFragment(SalesFragment())
                    true
                }
                R.id.fragment_product_register -> {
                    replaceFragment(ProductRegisterFragment())
                    true
                }
                R.id.fragment_purchase_history -> {
                    replaceFragment(PurchaaseHistory())
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view_tag, fragment)
            .commit()
    }
}