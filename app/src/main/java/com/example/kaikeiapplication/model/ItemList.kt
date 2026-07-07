package com.example.kaikeiapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**これは、販売する商品をデータクラスになります**/
@Entity(tableName = "items")//Room用のデータベース名を定義する！
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, //商品名
    val stock: Int,//在庫
    val price: Int,//値段
    val description: String?, //説明
    val isActivity: Boolean = true, //削除されているかどうかの判定
    val barcode: String?,
    val createdAt: Long, //作成した日
    val updatedAt: Long, //編集した日
    val deletedAt: Long? //削除した日
)