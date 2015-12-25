package com.example.staring.qianghongbao;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by staring on 2015/12/24.
 */
public class QiangHongBaoService extends AccessibilityService {

    /*
    问题1：在聊天界面的时候不能主动抢红包 因为我暂时还没找到事件改变
    问题2：锁屏状态下不能 解决办法 直接不让锁屏了
    window内容改变了 如果红包数>0 我就去抢 否则什么都不干
    * */

    static final String TAG = "QiangHongBao";

    // 记录可以抢的红包个数 初始的时候为0
    static int red_envelope = 0;
    //    是不是notification过来的红包
    private static boolean isfromNotification = false;

    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 红包消息的关键字
     */
    static final String HONGBAO_TEXT_KEY = "[微信红包]";

    Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        unlockScreen();//不锁屏
    }

    private void unlockScreen() {
//        不要锁屏不要黑屏
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService
                (Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyguardLock =
                keyguardManager.newKeyguardLock("MyKeyguardLock");
        keyguardLock.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService
                (Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock
                (PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");

        wakeLock.acquire();
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

//        Log.d(TAG, "事件---->" + event);

        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY)) {
                        red_envelope++;  //微信红包加1
                        isfromNotification = true;   //表示从notification过来的
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            微信整个界面就是一个window
            openHongBao(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            Log.e("staring", red_envelope + "");
//            有红包并且 内容改变的view是FrameLayout
            if (red_envelope > 0 && "android.widget.FrameLayout".equals(event.getClassName())) {
                openHongBao(event);
            }
        }
    }


    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        Log.e("staring", event.getClassName().toString());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //点中了红包，下一步就是去拆红包
            checkKey1();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后看详细的纪录界面
//            nothing
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            if (red_envelope > 0) {
//                代表有红包 我去点击 聊天界面最新的红包
                checkKey2();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
//        抢完红包后 红包个数减1
        if (isfromNotification) {
//            如果是从notification过来的 红包数要减1
            red_envelope--;
            isfromNotification = false;
        }

    }

    //还是这个方法又问题啊
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for (AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
