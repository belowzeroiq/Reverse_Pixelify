package com.uragiristereo.reversepixelify

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedBridge

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    companion object {
        const val TAG = "ReversePixelify"

        val pixelPropsUtilClasses = listOf(
            PixelPropsUtilClass(
                rom = "PixelExperience",
                className = "com.android.internal.util.custom.PixelPropsUtils",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "PixelOS",
                className = "com.android.internal.util.PropImitationHooks",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "ThePixelProject",
                className = "com.android.internal.util.PropImitationHooks",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "EvolutionX",
                className = "com.android.internal.util.evolution.PixelPropsUtils",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "CrDroidAndroid",
                className = "com.android.internal.util.crdroid.PixelPropsUtils",
                methodName = "setProps",
            ),
        )
    }

    private val pixelPropUtilClass = getPixelPropsUtilClass()

    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG

        if (pixelPropUtilClass == null) {
            log("Your ROM doesn't have internal spoofing or unsupported.")
        }
    }

    override fun onHook() = encase {
        if (pixelPropUtilClass != null) {
            loadApp {
                // New member-based hook syntax
                findClass(pixelPropUtilClass.className).hook {
                    method {
                        name = pixelPropUtilClass.methodName
                        emptyParam()
                    }.before {
                        log("Revert spoofing for $packageName")
                        result = null
                    }
                }
            }
        }
    }

    private fun getPixelPropsUtilClass(): PixelPropsUtilClass? {
        for (clazz in pixelPropsUtilClasses) {
            try {
                clazz.className.toClass()
                return clazz
            } catch (_: NoClassDefFoundError) { }
        }
        return null
    }

    private fun log(message: String) {
        XposedBridge.log("[$TAG] $message")
        Log.i(TAG, message)
    }
}
