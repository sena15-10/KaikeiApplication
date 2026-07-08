package com.example.kaikeiapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.kaikeiapplication.model.Product

/**
 * DAO (Data Access Object)
 * データベースへの「操作」を定義するインターフェースです。
 *
 * 初心者の方へ：
 * このインターフェースは、データベース（情報の貯蔵庫）に対して
 * 「保存して！」「消して！」「これ探して！」といった注文を出すための「窓口」です。
 */
@Dao
interface RegistrationDao {

    /**
     * 【登録】新しいデータをデータベースに保存します。
     * @param product 保存したい商品のデータ（1件分）
     *
     * suspend（サスペンド）:
     * データベースの操作は時間がかかることがあるため、スマホの画面が固まらないように
     * 「裏側（バックグラウンド）」でこっそり実行するための印です。
     */

    @Insert
    suspend fun insert(product : Product)

    /**
     * 【更新】すでにあるデータの内容を最新の状態に書き換えます。
     * 例えば、商品の名前や価格を修正した時に使います。
     */
    @Update
    suspend fun update(item: Product)

    /**
     * 【削除】指定したデータをデータベースから完全に消去します。
     */
    @Delete
    suspend fun delete(item: Product)
    // ★ここを追加する！全商品を取得するメソッド
    // LiveDataにすると、データが変わったとき自動で画面が更新される
    @Query("SELECT * FROM items")
    fun getAllProducts(): LiveData<List<Product>>
    /**
     * 【検索・取得】特定の条件に合うデータをデータベースから取り出します。
     *
     * @Query("...") : ここに「SQL」というデータベース専用の命令文を書きます。
     * 意味：「itemsテーブルから、在庫(stock)が指定された数(:threshold)以下のものを全部取ってきて」
     *
     * LiveData（ライブデータ）:
     * 「データの見張り番」です。データベースの中身が変わると、自動的に画面に通知してくれる
     * 非常に便利な仕組みです。これを使うと、画面を再読み込みしなくても自動でリストが更新されます。
     *
     * @param threshold 「在庫が残りわずか」と判断する基準の数
     * @return 条件に一致する商品のリスト（自動監視機能付き）
     */
    @Query("SELECT * FROM items WHERE stock <= :threshold")
    fun getLowStockItems(threshold: Int): LiveData<List<Product>>
}