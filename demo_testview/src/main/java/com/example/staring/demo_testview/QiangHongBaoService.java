package com.example.staring.demo_testview;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/*存在问题 我无法知道那个红包是否真的没用了*/

import java.util.List;

/**
 * Created by staring on 2015/12/24.
 */
public class QiangHongBaoService extends AccessibilityService {

    private static final String TAG = "staring";
    private static final int TYPE_HAVE = 0;
    private static final int TYPE_CHAI = 1;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "事件---->" + event);
//        微信主页 进入一次会触发window-state-change 之后就是content-change
        final int eventType = event.getEventType();
//        都是同一个视图来的
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                window状态改变了
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            window内容改变了
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//            Log.e("staring", nodeInfo.toString());
//            每次都去遍历一次很明显不是很好的做法
            recycle(nodeInfo);
        }
    }


    /**
     * @param info 当前节点
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info != null) {
            if (info.getChildCount() == 0) {
                CharSequence text = info.getText();
//                打开红包
                if (text != null && "微信红包".equals(text.toString().trim())) {
                    Log.e(TAG, info.toString());
                    Toast.makeText(this, "微信红包", Toast.LENGTH_SHORT).show();
                    List<AccessibilityNodeInfo> list = getRootInActiveWindow().findAccessibilityNodeInfosByText("你已领取了");
                    if (list == null) {
                        info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }

                }

//                拆红包
                if (text != null && "拆红包".equals(text.toString().trim())) {
                    Log.e(TAG, info.toString());
//                    如何判断那个红包是否已经拆了 如果存在 “你已领取了”什么字样代表
                    Toast.makeText(this, "拆红包", Toast.LENGTH_SHORT).show();
                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            } else {
                int size = info.getChildCount();
                for (int i = 0; i < size; i++) {
                    AccessibilityNodeInfo childInfo = info.getChild(i);
                    if (childInfo != null) {
//                        Log.e(TAG, "index: " + i + " info" + childInfo.getClassName() + " : " + childInfo.getContentDescription() + " : " + info.getText());
                        recycle(childInfo);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
