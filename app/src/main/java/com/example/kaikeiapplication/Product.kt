package com.example.kaikeiapplication

data class Product(
    val id: Int, //識別子(データベース使用時)
    val name: String,//商品名
    val price: Int,//単価
    var quantity: Int,//カート入れた数量
    val stock: Int//在庫数
)
