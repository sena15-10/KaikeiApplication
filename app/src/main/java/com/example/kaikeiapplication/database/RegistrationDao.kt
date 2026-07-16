package com.example.kaikeiapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.kaikeiapplication.model.Product

@Dao
interface RegistrationDao {

    /**
     * 【登録】新しいデータをデータベースに保存します。
     */
    @Insert
    suspend fun insert(product: Product)

    /**
     * 【更新】すでにあるデータの内容を最新の状態に書き換えます。
     */
    @Update
    suspend fun update(product: Product)

    /**
     * 【削除】指定したデータをデータベースから完全に消去します。
     */
    @Delete
    suspend fun delete(product: Product)

    /**
     * 【全取得】登録されているすべての商品を所得します。
     * LiveDataを使用しているため、データに変更があると自動で通知されます。
     */
    @Query("SELECT * FROM items")
    fun getAllProducts(): LiveData<List<Product>>

    /**
     * 【在庫検索】在庫が指定された数以下のものを取得します。
     */
    @Query("SELECT * FROM items WHERE stock <= :threshold")
    fun getLowStockItems(threshold: Int): LiveData<List<Product>>

    /**
     * 【ID検索】指定されたIDの商品を1件取得します。
     */
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    /**
     * 【商品名検索】商品名から商品を1件取得します。
     * 売上履歴(SalesItem)は商品IDではなく商品名しか持っていないため、
     * 売上削除時に在庫を戻す商品を特定するのに使用します。
     */
    @Query("SELECT * FROM items WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): Product?
}