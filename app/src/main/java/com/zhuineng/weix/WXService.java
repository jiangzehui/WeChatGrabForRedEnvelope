package com.zhuineng.weix;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quxianglin on 16/12/7.
 */
public class WXService extends AccessibilityService {
    //    disableSelf()：禁用当前服务，也就是在服务可以通过该方法停止运行
//    findFoucs(int falg)：查找拥有特定焦点类型的控件
//    getRootInActiveWindow()：如果配置能够获取窗口内容,则会返回当前活动窗口的根结点
//    getSeviceInfo()：获取当前服务的配置信息
//    onAccessibilityEvent(AccessibilityEvent event)：有关AccessibilityEvent事件的回调函数，系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
//    performGlobalAction(int action)：执行全局操作，比如返回，回到主页，打开最近等操作
//    setServiceInfo(AccessibilityServiceInfo info)：设置当前服务的配置信息
//    getSystemService(String name)：获取系统服务
//    onKeyEvent(KeyEvent event)：如果允许服务监听按键操作，该方法是按键事件的回调，需要注意，这个过程发生了系统处理按键事件之前
//    onServiceConnected()：系统成功绑定该服务时被触发，也就是当你在设置中开启相应的服务，系统成功的绑定了该服务时会触发，通常我们可以在这里做一些初始化操作
//    onInterrupt()：服务中断时的回调
    private List<AccessibilityNodeInfo> parents;
    private boolean flag = false;//判断是否是抢完红包后返回的
//    int lastNum = 0;
//    boolean bool =true;

    /**
     * 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建，比较不错，但是不能保证任何情况下都被重建，比如进程被干掉了。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setWhen(System.currentTimeMillis())
                .setTicker("哈哈")
                .setContentTitle("微信自动抢红包alpha版")
                .setContentText("已开启自动抢红包功能")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
        /*使用startForeground,如果id为0，那么notification将不会显示*/
        startForeground(1, notification);

    }

    @Override
    public void onDestroy() {
        Log.i("wxservice", "服务已停止");
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        stopForeground(true);
        super.onDestroy();


    }

    /**
     * 当服务启动时会被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
        Log.i("wxservice", "服务已启动");
    }

    /**
     * 监听窗口变化回调
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();//根据事件回调类型，进行处理
        Log.e("demo", eventType + "");
        switch (eventType) {

//            case 2048:
//                String className = event.getClassName().toString();
//                Log.e("demo", className);
//
//                //点击最后一个红包
//                Log.e("demo", "查询是否有红包，如果有则自动点击红包");
//                if(bool){
//                    bool = false;
//                    getLastPacket();
//                }
//
//
//                break;

            //当通知栏发生改变时
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (content.contains("[微信红包]")) {
                            //模拟打开通知栏消息，即打开微信
                            if (event.getParcelableData() != null &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    wakeAndUnlock();
                                    pendingIntent.send();
                                    Log.e("demo", "进入微信");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            //当窗口的状态发生改变时
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String classNames = event.getClassName().toString();
                Log.e("demo", classNames);
                if (classNames.equals("com.tencent.mm.ui.LauncherUI")) {
                    //点击最后一个红包
                    Log.e("demo", "查询是否有红包，如果有则自动点击红包");
                    getLastPacket();
                } else if (classNames.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    //开红包
                    flag = true;
                    Log.e("demo", "开红包");
                    inputClick("com.tencent.mm:id/lucky_money_recieve_open");
                } else if (classNames.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    //退出红包
                    Log.e("demo", "退出红包");
                    //Home键
                    //performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    if (flag) {
                        Intent resolveIntent = getPackageManager().getLaunchIntentForPackage("com.zhuineng.weix");// 这里的packname就是从上面得到的目标apk的包名
                        startActivity(resolveIntent);
                        flag = false;
                    }


                }

                break;
        }

    }


    /**
     * 通过ID获取控件，并进行模拟点击
     *
     * @param clickId
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void inputClick(String clickId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 获取List中最后一个红包，并进行模拟点击
     */
    private void getLastPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        //parents.clear();
        recycle(rootNode);
        if (parents.size() > 0) {
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            parents = new ArrayList<>();
//            Log.e("demo", "lastNum=" + lastNum + "-----parents.size=" + parents.size());
//            if (lastNum < parents.size()) {
//
//                parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                lastNum = parents.size();
//                parents=new ArrayList<>();
//                bool = true;
//            }else{
//                parents=new ArrayList<>();
//                bool = true;
//            }


        } else {
//            parents=new ArrayList<>();
//            bool = true;
        }


    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info == null) {
            return;
        }

        if (info.getChildCount() == 0) {
            if (info.getText() != null) {

                if ("领取红包".equals(info.getText().toString())) {
                    Log.e("demo", info.getText().toString());
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }


    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;

    private void wakeAndUnlock() {
        if (MainActivity.isLock) {
            //获取电源管理器对象
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

            //点亮屏幕
            wl.acquire();

            //得到键盘锁管理器对象
            km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");

            //解锁
            kl.disableKeyguard();
        } else {
            //锁屏
            kl.reenableKeyguard();

            //释放wakeLock，关灯
            wl.release();
        }

    }


    /**
     * 中断服务回调
     */
    @Override
    public void onInterrupt() {

    }


}
