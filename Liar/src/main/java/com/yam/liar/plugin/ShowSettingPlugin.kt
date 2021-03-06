package com.yam.liar.plugin

import android.content.Intent
import com.yam.core.util.ActivityRequestCode
import com.yam.core.util.AppUtils
import com.yam.core.util.plugin.YPlugin
import com.yam.liar.view.activity.YNetworkActivity
import org.json.JSONObject

class ShowSettingPlugin : YPlugin() {
    val TAG = ShowSettingPlugin::class.simpleName

    var callback: String = ""
    lateinit var result: JSONObject

    override fun executePlugin() {
        result = JSONObject()

        if (param.has("callback")) {
            callback = param.getString("callback");
        }

        var intent = Intent(activity, YNetworkActivity::class.java)
        startActivityForResult(intent, ActivityRequestCode.REQUEST_CODE_NETWORK_SETTING)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)

        if(reqCode == ActivityRequestCode.REQUEST_CODE_NETWORK_SETTING){
            var isRefresh = false

            if(data != null) {
                isRefresh = data!!.getBooleanExtra("refresh", false)
            }

            if(isRefresh){
                AppUtils.restartApp(activity!!)
            }

            result.put("result", true)
            listener.sendCallback(callback, result)
        }
    }
}