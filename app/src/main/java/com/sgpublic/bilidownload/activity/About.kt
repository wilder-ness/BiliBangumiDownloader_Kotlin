package com.sgpublic.bilidownload.activity

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import com.kongzue.dialogx.dialogs.MessageDialog
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.databinding.ActivityAboutBinding
import com.sgpublic.bilidownload.module.UpdateModule
import com.sgpublic.bilidownload.util.ActivityCollector
import java.text.SimpleDateFormat
import java.util.*

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        setSupportActionBar(binding.aboutToolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            binding.aboutVersion.text = String.format(
                getString(R.string.text_version),
                packageManager.getPackageInfo(packageName, 0).versionName
            )
        } catch (ignore: PackageManager.NameNotFoundException) { }
        binding.aboutDeveloper.setOnClickListener {
            MessageDialog.build()
                .setTitle(R.string.title_about_developer_dialog)
                .setMessage(R.string.text_about_developer_dialog)
                .setOkButton(R.string.text_ok)
                .show()
        }
        binding.aboutFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/SGPublic/BiliBangumiDownloader_Kotlin/issues")
            startActivity(intent)
        }
        binding.aboutLicense.setOnClickListener {
            License.startActivity(this@About)
        }
        binding.aboutUpdate.setOnClickListener {
            onUpdate(0)
        }
        binding.aboutLogo.setOnLongClickListener {
            onUpdate(1)
            false
        }
    }

    private fun onUpdate(type: Int) {
        binding.aboutProgress.visibility = View.VISIBLE
        val helper = UpdateModule(this@About)
        helper.getUpdate(type, object : UpdateModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable) {
                runOnUiThread {
                    onToast(R.string.error_update, code)
                    binding.aboutProgress.visibility = View.GONE
                    CrashHandler.saveExplosion(e, code)
                }
            }

            override fun onUpToDate() {
                runOnUiThread {
                    binding.aboutProgress.visibility = View.GONE
                    onToast(R.string.title_update_already)
                }
            }

            override fun onUpdate(
                force: Int,
                verName: String,
                sizeString: String,
                changelog: String,
                dlUrl: String
            ) {
                runOnUiThread {
                    binding.aboutProgress.visibility = View.GONE
                    val updateHeader = intArrayOf(
                        R.string.text_update_content,
                        R.string.text_update_content_force
                    )
                    val builder =
                        AlertDialog.Builder(this@About)
                    builder.setTitle(R.string.title_update_get)
                    builder.setCancelable(force == 0)
                    builder.setMessage(
                        String.format(this@About.getString(updateHeader[force]), sizeString)
                                + "\n" + this@About.getString(R.string.text_update_version) + verName + "\n"
                                + this@About.getString(R.string.text_update_changelog) + "\n" + changelog
                    )
                    builder.setPositiveButton(R.string.text_ok) { _, _ ->
                        val url = Uri.parse(dlUrl)
                        val downloadManager =
                            applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        val req = DownloadManager.Request(url)
                        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        val apkName =
                            this@About.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk"
                        req.setDestinationInExternalFilesDir(
                            applicationContext,
                            Environment.DIRECTORY_DOWNLOADS,
                            apkName
                        )
                        req.setTitle(this@About.getString(R.string.title_update_download))
                        req.setMimeType("application/vnd.android.package-archive")
                        UpdateModule.listener(this@About, downloadManager.enqueue(req))
                    }
                    builder.setNegativeButton(R.string.text_cancel) { _, _ ->
                        if (force != 1) {
                            return@setNegativeButton
                        }
                        ActivityCollector.finishAll()
                    }
                    builder.show()
                }
            }

            override fun onDisabled(time: Long, reason: String) {
                val builder = AlertDialog.Builder(this@About)
                builder.setTitle(R.string.title_update_disable)
                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
                val date = Date()
                date.time = time
                builder.setMessage(
                    String.format(
                        this@About.getString(R.string.text_update_content_disable),
                        reason, sdf.format(date)
                    )
                )
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.text_ok) { _, _ -> ActivityCollector.finishAll() }
                runOnUiThread { builder.show() }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, About::class.java)
            context.startActivity(intent)
        }
    }

}