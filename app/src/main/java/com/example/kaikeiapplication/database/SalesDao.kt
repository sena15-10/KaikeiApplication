package com.example.kaikeiapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kaikeiapplication.model.SalesItem

@Dao
interface SalesDao {

    // --- 基本操作 ---

    @Insert
    suspend fun insert(salesItem: SalesItem)

    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): LiveData<List<SalesItem>>

    // --- 便利な使用例を追記 ---

    /**
     * 合計金額を計算するメソッド (History画面で「総売上」として表示できます)
     * 使用例: viewModel.totalAmount.observe(...) { sum -> textView.text = "合計: $sum" }
     */
    @Query("SELECT SUM(price * quantity) FROM sales")
    fun getTotalAmount(): LiveData<Int>

    /**
     * 最新の1件だけを取得する
     * 使用例: 「たった今購入したもの」を表示する場合
     */
    @Query("SELECT * FROM sales ORDER BY id DESC LIMIT 1")
    fun getLatestSale(): LiveData<SalesItem>
    /** 売り上げ履歴を任意削除するためにしている。
     * idsは、削除対象のIDのリスト**/
    @Query("DELETE FROM sales WHERE id IN (:ids)")
    suspend fun deleteItemByIds(ids: List<Int>)
    /**
     * 特定の期間の履歴を取得する（拡張性）
     * 使用例: 今日の履歴だけ、などの絞り込み
     */
    @Query("SELECT * FROM sales WHERE date = :dateString")
    fun getSalesByDate(dateString: String): LiveData<List<SalesItem>>

    @Query("DELETE FROM sales")
    suspend fun deleteAll()
}

/*
  【使い方のイメージ (ViewModel内など)】

  1. データの保存 (SalesFragmentの購入ボタン)
     val newItem = SalesItem(productName = "リンゴ", quantity = 2, price = 100, date = "2023/10/27")
     salesDao.insert(newItem) // これだけでDBに保存される

  2. データの表示 (HistoryFragment)
     salesDao.getAllSales().observe(viewLifecycleOwner) { list ->
         adapter.submitList(list) // DBが更新されると勝手にここが実行される！
     }
*/