package com.example.kaikeiapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kaikeiapplication.model.Product
import com.example.kaikeiapplication.model.SalesItem

/**
 * アプリ全体のデータベースを管理する抽象クラスです。
 * Roomライブラリを使用して、SQLiteデータベースの橋渡しを行います。
 *
 * @Database: 保存するエンティティ（データ構造）とバージョンを指定します。
 */
@Database(entities = [SalesItem::class, Product::class],
    version = 1,
    exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    /**
     * データ操作用オブジェクト(DAO)へのアクセス窓口です。
     * 実装はRoomが自動生成します。
     */
    abstract fun salesDao(): SalesDao
    abstract  fun registrationDao() : RegistrationDao
    companion object {
        /**
         * データベースのインスタンスを保持します。
         * @Volatile は、複数のスレッドから同時にアクセスされても値の整合性を保つための魔法です。
         */
        @Volatile 
        private var INSTANCE: AppDatabase? = null

        /**
         * データベースのインスタンスを取得します。
         * シングルトンパターンにより、アプリ全体で1つだけのインスタンスを使い回します。
         */
        fun getDatabase(context: Context): AppDatabase {
            // すでにインスタンスが存在すればそれを返し、なければ synchronized ブロックへ
            return INSTANCE ?: synchronized(this) {
                // 二重チェックを行い、他のスレッドが先に作っていないか確認した上で生成
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // 保存されるデータベースのファイル名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
