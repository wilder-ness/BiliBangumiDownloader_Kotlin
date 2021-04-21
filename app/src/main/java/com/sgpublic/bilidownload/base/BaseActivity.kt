package com.sgpublic.bilidownload.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.sgpublic.bilidownload.util.ActivityCollector
import com.yanzhenjie.sofia.Sofia
import me.imid.swipebacklayout.lib.SwipeBackLayout
import me.imid.swipebacklayout.lib.app.SwipeBackActivity
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class BaseActivity<T : ViewBinding>: SwipeBackActivity() {
    protected lateinit var binding: T

    private val edgeSize: Int = 200
    protected var rootViewBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCollector.addActivity(this)

        setSwipeBackEnable(onSetSwipeBackEnable())
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
        swipeBackLayout.setEdgeSize(edgeSize)

        getContentView()
        onViewSetup()
        onActivityCreated(savedInstanceState)
    }

    protected abstract fun onActivityCreated(savedInstanceState: Bundle?)

    @Suppress("UNCHECKED_CAST")
    private fun getContentView() {
        val parameterizedType: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        val clazz = parameterizedType.actualTypeArguments[0] as Class<T>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as T
        setContentView(binding.root)
    }

    protected open fun onSetSwipeBackEnable(): Boolean = false

    protected open fun initViewAtTop(view: View){
        var statusbarheight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }
        val params: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams
        params.topMargin = statusbarheight
    }

    protected open fun initViewAtBottom(view: View) {
        rootViewBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            var b = 0
            if (insets != null) {
                b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            }
            view.setPadding(
                view.paddingLeft, view.paddingTop, view.paddingRight, rootViewBottom + b
            )
            ViewCompat.onApplyWindowInsets(v, insets)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    protected open fun onViewSetup(){
        Sofia.with(this)
            .statusBarBackgroundAlpha(0)
            .navigationBarBackgroundAlpha(0)
            .invasionNavigationBar()
            .invasionStatusBar()
            .statusBarDarkFont()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    protected open fun setAnimateState(
        is_visible: Boolean,
        duration: Int,
        view: View,
        callback: Runnable? = null
    ) {
        runOnUiThread {
            if (is_visible) {
                view.visibility = View.VISIBLE
                view.animate().alphaBy(0f).alpha(1f).setDuration(duration.toLong())
                    .setListener(null)
                callback?.run()
            } else {
                view.animate().alphaBy(1f).alpha(0f).setDuration(duration.toLong())
                    .setListener(null)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        view.visibility = View.GONE
                        callback?.run()
                    }
                }, duration.toLong())
            }
        }
    }

    protected fun onToast(content: String?) {
        runOnUiThread {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }
    protected fun onToast(@StringRes content: Int) {
        onToast(resources.getText(content).toString())
    }
    protected fun onToast(@StringRes content: Int, code: Int) {
        val contentShow = (resources.getText(content).toString() + "($code)")
        onToast(contentShow)
    }
    protected fun onToast(@StringRes content: Int, message: String?, code: Int) {
        if (message != null) {
            val contentShow = resources.getText(content).toString() + "，$message($code)"
            onToast(contentShow)
        } else {
            onToast(content, code)
        }
    }

    protected fun dip2px(dpValue: Float): Int {
        val scales = resources.displayMetrics.density
        return (dpValue * scales + 0.5f).toInt()
    }
}