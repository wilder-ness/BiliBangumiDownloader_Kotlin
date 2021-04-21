package com.sgpublic.bilidownload.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityExceptionBinding
import com.sgpublic.bilidownload.util.ActivityCollector

class Catcher: BaseActivity<ActivityExceptionBinding>() {
    companion object {
        fun startActivity(context: Context, logContent: String){
           val intent = Intent(context, Catcher::class.java)
            intent.putExtra("logContent", logContent)
            context.startActivity(intent)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val logContent: String = intent.extras?.getString("logContent") ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        MessageDialog.build()
                .setTitle(R.string.title_function_crash)
                .setMessage(R.string.text_function_crash)
                .setCancelable(false)
                .setDialogLifecycleCallback(object : DialogLifecycleCallback<MessageDialog>() {
                    override fun onDismiss(dialog: MessageDialog?) {
                        super.onDismiss(dialog)
                        ActivityCollector.finishAll()
                    }
                })
                .setOkButton(R.string.text_function_crash_copy) { _, _ ->
                    val data = ClipData.newPlainText("Label", logContent)
                    clipboard.setPrimaryClip(data)
                    false
                }
                .setCancelButton(R.string.text_function_crash_exit)
                .show(this)
    }

    override fun onViewSetup() {
        super.onViewSetup()

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        title = ""
    }

    override fun getContentView(): ActivityExceptionBinding = ActivityExceptionBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = false
}