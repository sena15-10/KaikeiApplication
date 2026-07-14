package com.example.kaikeiapplication.saleshistory

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaikeiapplication.R
import com.example.kaikeiapplication.database.AppDatabase
import com.example.kaikeiapplication.model.SalesItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * 販売履歴画面を表示するためのフラグメントクラスです。
 * データベース（Room）から実際の販売履歴を取得し、ページネーション表示します。
 */
class PurchaaseHistory : Fragment() {

    // ── 定数・変数 ──────────────────────────────
    private val PAGE_SIZE = 5
    private var currentPage = 0
    private var salesList: List<SalesItem> = emptyList()

    private lateinit var adapter: SalesAdapter

    companion object {
        // 全削除の確認に使うパスワード
        private const val DELETE_ALL_PASSWORD = "PassWord123"
    }

    // ── ライフサイクル ───────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purchaase_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Viewの取得
        val rvSalesHistory = view.findViewById<RecyclerView>(R.id.rvSalesHistory)
        val btnPrev = view.findViewById<Button>(R.id.btnPrev)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val tvPageInfo = view.findViewById<TextView>(R.id.tvPageInfo)
        val btnOverflowMenu = view.findViewById<View>(R.id.btnOverflowMenu)
        val layoutPagination = view.findViewById<View>(R.id.layoutPagination)
        val layoutSelectionActionBar = view.findViewById<View>(R.id.layoutSelectionActionBar)
        val btnDeleteSelected = view.findViewById<Button>(R.id.btnDeleteSelected)

        // 2. RecyclerView の初期セットアップ
        adapter = SalesAdapter(emptyList()) { isSelectionMode, _ ->
            // ── STEP 4: BottomActionBarをVISIBLEにする ──
            // 理由: 選択モード中はページ送りの代わりに削除操作をできるようにするため
            layoutSelectionActionBar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            layoutPagination.visibility = if (isSelectionMode) View.GONE else View.VISIBLE
        }
        rvSalesHistory.layoutManager = LinearLayoutManager(requireContext())
        rvSalesHistory.adapter = adapter

        // 長押し選択モードの開始トリガー自体はAdapter側(itemViewのOnLongClickListener)で処理する。
        // ここでは選択削除の確認フローで使うContextMenuの窓口だけを用意しておく。
        registerForContextMenu(rvSalesHistory)

        // 3. リスナーの設定
        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                showPage(tvPageInfo, btnPrev, btnNext)
            }
        }

        btnNext.setOnClickListener {
            val totalPages = calcTotalPages()
            if (currentPage < totalPages - 1) {
                currentPage++
                showPage(tvPageInfo, btnPrev, btnNext)
            }
        }

        btnOverflowMenu.setOnClickListener { anchor ->
            // ── STEP 1: OverflowMenuのアイテムがタップされたか確認する ──
            // 理由: どのメニュー項目が選ばれたかによって処理を振り分けるため
            val popupMenu = PopupMenu(requireContext(), anchor)
            popupMenu.menuInflater.inflate(R.menu.menu_purchase_history, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete_all -> {
                        showDeleteAllPasswordDialog(view)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        btnDeleteSelected.setOnClickListener {
            // TODO: [別担当] "選択削除"ボタン押下後のContextMenuによる
            //              削除確認ダイアログを実装する
            //              → registerForContextMenu() / onContextItemSelected() を使用すること
            // TODO: [別担当] 削除実行後のリスト更新とAdapter通知を実装する
        }

        // 4. データベースからデータを取得して監視
        val dao = AppDatabase.Companion.getDatabase(requireContext()).salesDao()

        // getAllSales() は LiveData<List<SalesItem>> を返すので、データが変わるたびに実行される
        dao.getAllSales().observe(viewLifecycleOwner) { updatedList ->
            salesList = updatedList

            // データ件数が減ってページが範囲外になった場合は補正する
            val totalPages = calcTotalPages()
            if (currentPage > totalPages - 1) {
                currentPage = totalPages - 1
            }

            showPage(tvPageInfo, btnPrev, btnNext)
            updateSalesSummary(view, salesList)
        }
    }

    // ── OverflowMenu: 全削除（パスワード認証） ───────────

    private fun showDeleteAllPasswordDialog(rootView: View) {
        // ── STEP 2: パスワード入力ダイアログを生成する ──
        // 理由: 誤操作による全件削除を防ぐため、パスワードによる確認を挟む
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_confirm, null)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etPassword)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialogTitleDeleteAll)
            .setMessage(R.string.dialogMessageDeleteAll)
            .setView(dialogView)
            .setPositiveButton(R.string.btnDelete) { _, _ ->
                // ── STEP 3: 入力値と正解パスワードを比較する ──
                // 理由: 一致しない限りデータを削除しないようにするため
                val inputPassword = etPassword.text?.toString().orEmpty()
                if (inputPassword == DELETE_ALL_PASSWORD) {
                    // ── STEP 4: 一致 → リスト全件を削除してAdapterに通知する ──
                    // 理由: データの実体はRoom(DB)にあるため、Daoで全件削除し、
                    //       画面のAdapterはgetAllSales()のLiveData監視結果で自動更新させる
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.Companion.getDatabase(requireContext()).salesDao().deleteAll()
                    }
                } else {
                    // ── STEP 5: 不一致 → エラーメッセージを表示して何もしない ──
                    Snackbar.make(rootView, R.string.errorPasswordMismatch, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btnCancel, null)
            .show()
    }

    // ── 選択削除フロー用のContextMenuの窓口 ──────────────

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // TODO: [別担当] "選択削除"ボタン押下後のContextMenuによる
        //              削除確認ダイアログを実装する
        //              → ここに確認用のMenuItem（例:「削除する」「キャンセル」）を追加する
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // TODO: [別担当] 選択されたMenuItemに応じて選択中アイテムの削除確認処理を実行する
        // TODO: [別担当] 削除実行後のリスト更新とAdapter通知を実装する
        return super.onContextItemSelected(item)
    }

    // ── ヘルパーメソッド ─────────────────────────

    private fun showPage(tvPageInfo: TextView, btnPrev: Button, btnNext: Button) {
        val totalPages = calcTotalPages()

        // 表示範囲の計算
        val fromIndex = currentPage * PAGE_SIZE
        val toIndex = minOf(fromIndex + PAGE_SIZE, salesList.size)

        // リストが空でない場合のみsubListを取得
        val currentDisplayList = if (salesList.isNotEmpty()) {
            salesList.subList(fromIndex, toIndex)
        } else {
            emptyList()
        }

        // Adapterの更新
        adapter.updateData(currentDisplayList)

        // UI更新
        tvPageInfo.text = "${currentPage + 1} / $totalPages ページ"
        btnPrev.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < totalPages - 1
    }

    private fun calcTotalPages(): Int {
        if (salesList.isEmpty()) return 1
        return ceil(salesList.size.toDouble() / PAGE_SIZE).toInt()
    }

    /**
     * データベースから取得したリストに基づいて集計を計算し、UIに反映します。
     */
    private fun updateSalesSummary(rootView: View, salesList: List<SalesItem>) {
        val totalAmount = salesList.sumOf { it.quantity * it.price }
        val totalQuantity = salesList.sumOf { it.quantity }
        val transactionCount = salesList.size

        rootView.findViewById<TextView>(R.id.tvSumSales)?.text = totalAmount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesCount)?.text = transactionCount.toString()
        rootView.findViewById<TextView>(R.id.tvSalesNum)?.text = totalQuantity.toString()
    }
}
