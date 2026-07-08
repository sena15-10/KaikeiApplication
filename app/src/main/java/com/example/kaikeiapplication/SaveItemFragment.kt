package com.example.kaikeiapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.kaikeiapplication.database.AppDatabase
import com.example.kaikeiapplication.model.Product
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SaveItemFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_save_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        引数からidを取得
        val productId = arguments?.getInt("productId")
        val db = AppDatabase.getDatabase(requireContext())
        val nameInput = view.findViewById<TextInputEditText>(R.id.takoyaki)
        val priceInput = view.findViewById<TextInputEditText>(R.id.kingaku)
        val countInput = view.findViewById<TextInputEditText>(R.id.zaikosuu)


        (activity as? MainActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "商品設定"
        }
        val itemName = view.findViewById<TextInputEditText>(R.id.takoyaki)
        val itemPrice = view.findViewById<TextInputEditText>(R.id.kingaku)
        val itemCount = view.findViewById<TextInputEditText>(R.id.zaikosuu)
        val registerButton = view.findViewById<Button>(R.id.registration)

        //編集モードの初期設定
        if (productId != null) {
            //Idがあったら編集用ボタン
            registerButton.text = "変更を登録"

            lifecycleScope.launch {
                val product = withContext(Dispatchers.IO) {
                    db.registrationDao().getProductById(productId)
                }
                product?.let{
                    nameInput.setText(it.name)
                    priceInput.setText(it.price.toString())
                    countInput.setText(it.stock.toString())
                }
            }
        }

//        商品設定欄が3つとも埋まっていないと登録できない
        registerButton.isEnabled = false
        //登録ボタン押されたときの処理
        registerButton?.setOnClickListener {

            val name = itemName.text.toString().trim()
            val price = itemPrice.text.toString().toIntOrNull() ?: 0
            val stock = itemCount.text.toString().toIntOrNull() ?: 0

            //productIdの有無で処理を分岐
            val isEditMode = productId != null && productId != 0

            lifecycleScope.launch(Dispatchers.IO){
                val db = AppDatabase.getDatabase(requireContext())
                if (isEditMode){
                    // 【更新処理】既存のIDを指定してProductを作成
                    val updatedItem = Product(
                        id = productId!!, // ここで既存のIDを渡すのが重要！
                        name = name,
                        price = price,
                        stock = stock,
                        description = null,
                        barcode = null,
                        createdAt = System.currentTimeMillis(), // 本来は取得した値を入れるのが望ましい
                        updatedAt = System.currentTimeMillis(),
                        deletedAt = null
                    )
                    // Daoに作成した @Update メソッドを呼ぶ
                    db.registrationDao().update(updatedItem)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "変更を保存しました", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val newItem = Product(
                        name = name,
                        price = price,
                        stock = stock,
                        description = null, // 今回はなし
                        barcode = null,     // 今回はなし
                        createdAt = System.currentTimeMillis(), // 作成した時間
                        updatedAt = System.currentTimeMillis(), // 更新した時間
                        deletedAt = null
                    )

//                    //データベースの保存(重たい処理はlifecycleScopeで非同期処理(裏側処理))
//                    lifecycleScope.launch(Dispatchers.IO){
//                        val db = AppDatabase.getDatabase(requireContext())
                        db.registrationDao().insert(newItem)



                }
                //４，保存が終わったら、元の画面に戻る(画面操作はメインスレッドで行う)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "${name}を登録しました", Toast.LENGTH_SHORT).show()

                    (activity as? MainActivity)?.replaceFragment(ProductRegisterFragment())
                }
                (activity as? MainActivity)?.replaceFragment(ProductList())
            }




        }
        fun checkInputs() {
            val nameText = itemName.text.toString().trim()
            val priceText = itemPrice.text.toString().trim()
            val countText = itemCount.text.toString().trim()

            val isBothFilled =nameText.isNotEmpty() && priceText.isNotEmpty() && countText.isNotEmpty()

            registerButton.isEnabled = isBothFilled
        }
        itemName.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }
        itemPrice.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }
        itemCount.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }

    }

//    private inner class RegisterButtonClickListener : View.OnClickListener {
//        override fun onClick(v: View?) {
//
//    }
}