package com.example.kaikeiapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DeleteDialogFragment : DialogFragment() {

    // 削除が確定した時に呼ばれるコールバック
    var onConfirmDelete: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(R.string.tvdelete)

        // 「はい」ボタン
        builder.setPositiveButton(R.string.yesBtn) { _, _ ->
            onConfirmDelete?.invoke() // セットされた処理を実行
        }

        // 「いいえ」ボタン
        builder.setNegativeButton(R.string.noBtn) { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }
}