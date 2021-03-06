package com.yam.core.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.yam.core.application.YApplication
import com.yam.core.util.ActivityRequestCode
import com.yam.core.util.AppUtils
import com.yam.core.util.RUtil
import com.yam.core.util.plugin.CompleteListener
import com.yam.core.util.plugin.YBridge
import com.yam.core.util.plugin.YPlugin
import org.json.JSONObject

abstract class YFragment : Fragment() {
    lateinit var wrapper: View
    private var runningTask: YPlugin? = null

    var touched = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        YApplication.addFragment(this)

        wrapper.isClickable = true

        onInit()
    }

    /**
     * onActivityCreated() 에서 호출해주는 메서드로,
     * 화면이 이동되고 최초로 호출되는 메서드(선택적으로 사용)
     */
    abstract fun onInit()

    /**
     * Fragment 간 이동하는 메서드로,
     * 애니메이션을 지정해 줄 수 있음
     */
    open fun moveToFragment(fragment: Fragment, anim: String) {
        var fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()

        if(!TextUtils.isEmpty(anim)) {
            when(anim){
                "top" -> fragmentTransaction.setCustomAnimations(RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_in_bottom"), RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_out_top"))
                "left" -> fragmentTransaction.setCustomAnimations(RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_in_right"), RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_out_left"))
                "right" -> fragmentTransaction.setCustomAnimations(RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_in_left"), RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_out_right"))
                "bottom" -> fragmentTransaction.setCustomAnimations(RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_in_top"), RUtil.getR(activity!!.applicationContext, "anim", "anim_slide_out_bottom"))
                "fade" -> fragmentTransaction.setCustomAnimations(RUtil.getR(activity!!.applicationContext, "anim", "anim_fade_in"), RUtil.getR(activity!!.applicationContext, "anim", "anim_fade_out"))
            }
        }

        var preFragment = YApplication.fragmentList.get(YApplication.fragmentList.size - 1)
        preFragment.onHiddenChanged(true)

        fragmentTransaction.add(
            RUtil.getIdR(activity!!.applicationContext, "contents"),
            fragment,
            "center"
        )
        fragmentTransaction.commitAllowingStateLoss()
    }

    open fun dispatchTouchEvent(cb: Window.Callback, event: MotionEvent) : Boolean {
        if(event.pointerCount == 3 && touched == false){
            touched = true
            if(YApplication.useSettingView){
                var data = JSONObject()
                data.put("id", "SHOW_SETTING_VIEW")
                data.put("param", JSONObject())

                YBridge.getInstance().callPluginFromApp("SHOW_SETTING_VIEW", data, object : CompleteListener {
                    override fun sendCallback(callback: String, resultData: JSONObject) {
                        touched = false
                    }
                })
            }
        }

        return cb.dispatchTouchEvent(event)
    }

    fun startActivityForResultFromPlugin(task: YPlugin, intent: Intent, requestCode: Int) {
        runningTask = task
        super.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(runningTask != null){
            runningTask!!.onActivityResult(requestCode, resultCode, data)
            runningTask = null
        }
    }
}