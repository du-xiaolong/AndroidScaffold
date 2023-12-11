package com.dxl.androidscaffold.assist

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.dxl.androidscaffold.databinding.ActivityAssistBinding

/**
 *
 * @author duxiaolong
 * @date 2023/12/11
 */
class AssistActivity : AppCompatActivity() {

    private lateinit var activityAssistBinding: ActivityAssistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityAssistBinding.inflate(layoutInflater).also { activityAssistBinding = it }.root
        )
        activityAssistBinding.btnPermission.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
        activityAssistBinding.btnStart.setOnClickListener {
//            startService(Intent(this, AutoAccessibilityService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val accessibilitySettingsOn =
            isAccessibilitySettingsOn(AutoAccessibilityService::class.java)
        activityAssistBinding.btnPermission.isEnabled = !accessibilitySettingsOn
        if (accessibilitySettingsOn) {
            activityAssistBinding.btnPermission.isEnabled = false
            activityAssistBinding.btnPermission.text = "已开启权限"
        }else{
            activityAssistBinding.btnPermission.isEnabled = true
            activityAssistBinding.btnPermission.text = "点击开启权限"
        }
    }

    fun Context.isAccessibilitySettingsOn(clazz: Class<out AccessibilityService?>): Boolean {
        var accessibilityEnabled = false    // 判断设备的无障碍功能是否可用
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled) {
            // 获取启用的无障碍服务
            val settingValue: String? = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                // 遍历判断是否包含我们的服务
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(
                            "${packageName}/${clazz.canonicalName}",
                            ignoreCase = true
                        )
                    ) return true

                }
            }
        }
        return false
    }

}