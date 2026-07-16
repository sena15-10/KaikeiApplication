package com.example.kaikeiapplication.saleshistory

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // 日付フィルターの入力値（nullまたは空文字なら未フィルター＝salesListをそのまま使う）
    private var activeDateFilter: String? = null

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
        val etDateFilter = view.findViewById<EditText>(R.id.etDateFilter)
        val btnApplyFilter = view.findViewById<Button>(R.id.btnApplyFilter)
        val btnClearFilter = view.findViewById<Button>(R.id.btnClearFilter)

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
                        // 全削除: パスワード確認後、削除する分の在庫を戻してからDBの全件を削除する
                        // (AdapterはgetAllSales()のLiveData監視結果で自動更新される)
                        val itemsToDelete = salesList
                        showPasswordConfirmDialog(view) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                restoreStockForDeletedSales(itemsToDelete)
                                AppDatabase.Companion.getDatabase(requireContext()).salesDao().deleteAll()
                            }
                        }
                        true
                    }
                    R.id.action_export_csv -> {
                        exportSalesToCsv(view)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // 日付フィルターの適用・解除
        btnApplyFilter.setOnClickListener {
            // ── STEP 2: salesListをfilter{}で検索する ──
            // 理由: 入力日付と一致する履歴だけをユーザーに見せるため（DB再取得はしない）
            activeDateFilter = etDateFilter.text?.toString()?.trim()
            currentPage = 0
            showPage(tvPageInfo, btnPrev, btnNext)
        }

        btnClearFilter.setOnClickListener {
            etDateFilter.text?.clear()
            activeDateFilter = null
            currentPage = 0
            showPage(tvPageInfo, btnPrev, btnNext)
        }

        btnDeleteSelected.setOnClickListener {
            // 1. 選択中のID一覧を取得する
            val selectedIds = adapter.getSelectedIds()
            if (selectedIds.isEmpty()) return@setOnClickListener

            // 2. 全削除と同じパスワード確認ダイアログを使い、選択されたIDだけを削除する
            val itemsToDelete = salesList.filter { selectedIds.contains(it.id) }
            showPasswordConfirmDialog(view) {
                lifecycleScope.launch(Dispatchers.IO) {
                    // 削除する分の在庫を戻してから、選択された行だけを削除する
                    restoreStockForDeletedSales(itemsToDelete)
                    AppDatabase.Companion.getDatabase(requireContext()).salesDao()
                        .deleteItemByIds(selectedIds.toList())
                }
                // 削除後は選択モードを終了する（一覧はLiveDataの監視で自動更新される）
                adapter.exitSelectionMode()
            }
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

    // ── パスワード確認ダイアログ（全削除・選択削除で共通利用） ───────

    /**
     * パスワード入力ダイアログを表示し、正解であれば[onPasswordCorrect]を実行する。
     * 全削除・選択削除のどちらも、誤操作でデータを消さないようにこの確認を必ず通す。
     */
    private fun showPasswordConfirmDialog(rootView: View, onPasswordCorrect: () -> Unit) {
        // ── STEP 2: パスワード入力ダイアログを生成する ──
        // 理由: 誤操作によるデータ削除を防ぐため、パスワードによる確認を挟む
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
                    // ── STEP 4: 一致 → 呼び出し元の削除処理を実行する ──
                    onPasswordCorrect()
                } else {
                    // ── STEP 5: 不一致 → エラーメッセージを表示して何もしない ──
                    Snackbar.make(rootView, R.string.errorPasswordMismatch, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btnCancel, null)
            .show()
    }

    // ── 削除時の在庫復元 ─────────────────────────────

    /**
     * 削除される売上履歴の分だけ、対応する商品の在庫を元に戻す。
     * 理由: 売上を削除しても在庫がそのままだと、実際の在庫数と帳簿上の数が食い違ってしまうため。
     * SalesItemは商品IDを持たず商品名しか保持していないため、商品名でProductを検索して更新する。
     */
    private suspend fun restoreStockForDeletedSales(items: List<SalesItem>) {
        val registrationDao = AppDatabase.Companion.getDatabase(requireContext()).registrationDao()
        items.forEach { item ->
            val product = registrationDao.getProductByName(item.productName)
            if (product != null) {
                product.stock += item.quantity
                registrationDao.update(product)
            }
        }
    }

    // ── OverflowMenu: CSVエクスポート ────────────────────

    private fun exportSalesToCsv(rootView: View) {
        // API 29未満はMediaStoreのDownloadsコレクションが使えないため非対応とする
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Snackbar.make(rootView, R.string.csvExportUnsupportedVersion, Snackbar.LENGTH_SHORT).show()
            return
        }

        // ── STEP 1: エクスポートするリストを決定する（フィルター中ならフィルター後のリスト、
        //            そうでなければsalesList）──
        // 理由: 画面に表示されている条件のままエクスポートし、ユーザーの意図とズレないようにするため
        val exportList = currentSourceList()

        if (exportList.isEmpty()) {
            Snackbar.make(rootView, R.string.csvExportFailure, Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ── STEP 2: CSVのヘッダー行を生成する ──
                // 理由: どの列が何を表すか、ファイルを開いた人にもわかるようにするため
                val csvBuilder = StringBuilder()
                csvBuilder.append("商品名,数量,単価(円),合計金額(円),日付\n")

                // ── STEP 3: リストの各SalesItemを1行のCSV文字列に変換する ──
                // ── STEP 4: 全行をまとめてStringBuilderで結合する ──
                // 理由: 合計金額はDBに保存されていないため、quantity×priceをここで計算する
                exportList.forEach { item ->
                    val totalPrice = item.quantity * item.price
                    csvBuilder.append(
                        "${csvEscape(item.productName)},${item.quantity},${item.price}," +
                            "$totalPrice,${csvEscape(item.date)}\n"
                    )
                }

                val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "sales_$timeStamp.csv"

                // ── STEP 5: MediaStoreAPIを使ってDownloadsフォルダに書き込む ──
                // 理由: Android 10以降のスコープドストレージ下でも、
                //       WRITE_EXTERNAL_STORAGE権限なしで自アプリ作成ファイルを保存できるため
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Uriの作成に失敗しました")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvBuilder.toString().toByteArray())
                } ?: throw IllegalStateException("OutputStreamの作成に失敗しました")

                withContext(Dispatchers.Main) {
                    // ── STEP 6: 成功したらSnackbarでファイル名を通知する ──
                    Snackbar.make(rootView, getString(R.string.csvExportSuccess, fileName), Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(rootView, R.string.csvExportFailure, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * カンマ・ダブルクォート・改行を含むフィールドをCSVとして壊れないようにクォートで囲む
     */
    private fun csvEscape(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
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
        val sourceList = currentSourceList()
        val totalPages = calcTotalPages()

        // 表示範囲の計算
        val fromIndex = currentPage * PAGE_SIZE
        val toIndex = minOf(fromIndex + PAGE_SIZE, sourceList.size)

        // リストが空でない場合のみsubListを取得
        val currentDisplayList = if (sourceList.isNotEmpty()) {
            sourceList.subList(fromIndex, toIndex)
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
        val sourceList = currentSourceList()
        if (sourceList.isEmpty()) return 1
        return ceil(sourceList.size.toDouble() / PAGE_SIZE).toInt()
    }

    /**
     * 日付フィルターが有効ならフィルター後のリストを、無効ならsalesListをそのまま返す。
     * salesList自体は書き換えず、フィルター結果はここで都度計算する
     * （こうしておくとDB更新のたびに古いフィルター結果が残る心配がない）。
     */
    private fun currentSourceList(): List<SalesItem> {
        val filter = activeDateFilter
        return if (filter.isNullOrEmpty()) {
            salesList
        } else {
            salesList.filter { it.date.startsWith(filter) }
        }
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
