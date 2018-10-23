package com.lsw.hookpms;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Proxy;

/**
 * Created by sweeneyliu on 2018/10/22.
 */
public final class HookHelper {

    public static void hookActivityManager(Context context) {
        try {
            // 获取全局的ActivityThread对象
            Object currentActivityThread = RefInvoke.getFieldObject("android.app.ActivityThread", null,"sCurrentActivityThread");

            // 获取ActivityThread里面原始的 sPackageManager
            Object sPackageManager = RefInvoke.getFieldObject("android.app.ActivityThread", currentActivityThread,"sPackageManager");


            // 准备好代理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                    new Class<?>[] { iPackageManagerInterface },
                    new HookHandler(sPackageManager));

            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            RefInvoke.setFieldObject("android.app.ActivityThread",currentActivityThread, "sPackageManager", proxy);

            // 2. 替换 ApplicationPackageManager里面的 mPm对象
            PackageManager pm = context.getPackageManager();
            RefInvoke.setFieldObject("android.app.ApplicationPackageManager",pm, "mPM", proxy);

        } catch (Exception e) {
            throw new RuntimeException("Hook Failed", e);
        }
    }
}
