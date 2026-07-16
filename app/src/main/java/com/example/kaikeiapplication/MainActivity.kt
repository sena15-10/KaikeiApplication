package com.example.kaikeiapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.kaikeiapplication.saleshistory.PurchaaseHistory
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        ViewCompat.setOnApplyWindowInsetsListener(navView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }

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
                    replaceFragment(ProductList())
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
    public fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view_tag, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // フラグメントの履歴（バックスタック）があれば、前のフラグメントに戻る
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish() // 履歴がなければアクティビティを閉じる
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}