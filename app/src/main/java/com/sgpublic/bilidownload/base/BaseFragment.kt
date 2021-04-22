package com.sgpublic.bilidownload.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<T: ViewBinding>(private val contest: AppCompatActivity) : Fragment() {
    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = setupContentView(inflater, container)
        return binding.root
    }

    final override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onViewSetup()
        onFragmentCreated(savedInstanceState)
    }

    protected abstract fun onFragmentCreated(savedInstanceState: Bundle?)

    protected fun dip2px(dpValue: Float): Int {
        val scales = contest.resources.displayMetrics.density
        return (dpValue * scales + 0.5f).toInt()
    }

    protected fun runOnUiThread(runnable: () -> Unit){
        contest.runOnUiThread(runnable)
    }

    protected fun finish(){
        contest.finish()
    }

    protected fun <T: View?> findViewById(@IdRes res: Int): T? {
        return view?.findViewById<T>(res)
    }

    open fun getTitle(): CharSequence = ""

    protected open fun initViewAtTop(view: View){
        var statusbarheight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }
        val params = try {
            view.layoutParams as LinearLayout.LayoutParams
        } catch (e: ClassCastException) {
            view.layoutParams as FrameLayout.LayoutParams
        }
        params.topMargin = statusbarheight
    }

    @Suppress("UNCHECKED_CAST")
    private fun setupContentView(inflater: LayoutInflater, container: ViewGroup?): T {
        val parameterizedType: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        val clazz = parameterizedType.actualTypeArguments[0] as Class<T>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        return method.invoke(null, inflater, container, false) as T
    }

    protected open fun onViewSetup(){ }

    protected fun onToast(content: String?) {
        runOnUiThread {
            Toast.makeText(contest, content, Toast.LENGTH_SHORT).show()
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
}