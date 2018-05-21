package cn.aorise.webrtc.chat;

import android.content.Context;
import android.util.Log;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.common.inter.ITagManager;
import com.umeng.message.tag.TagManager;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.mezu.MeizuRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import java.util.Hashtable;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/21
 *     desc   : 推送帮助类
 *     version: 1.0
 * </pre>
 */
public class PushHelper {
    private static final String TAG = PushHelper.class.getSimpleName();
    private static PushHelper instance;

    private PushHelper() {
    }

    public static PushHelper getInstance() {
        if (instance == null) {
            synchronized (PushHelper.class) {
                if (instance == null) {
                    instance = new PushHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化友盟推送
     * 在AndroidManifest.xml中配置appkey和channel值，可以调用此版本初始化函数。
     *
     * @param context
     * @param pushSecret 友盟pushSecret
     */
    public void init(Context context, String pushSecret) {
        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(context, UMConfigure.DEVICE_TYPE_PHONE,
                pushSecret);
    }

    /**
     * 初始化友盟推送
     *
     * @param context
     * @param appkey     友盟AppKey
     * @param channel    推送类型
     * @param pushSecret 友盟pushSecret
     */
    public void init(Context context, String appkey, String channel, String pushSecret) {
        Log.e(TAG, "init: " + context.toString());
        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(context, appkey, channel, UMConfigure.DEVICE_TYPE_PHONE,
                pushSecret);
    }

    /**
     * 设置日志开关
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        UMConfigure.setLogEnabled(enabled);
    }

    /**
     * PushSDK初始化(如使用推送SDK，必须调用此方法
     */
    public void register(Context context) {
        //注册推送服务，每次调用register方法都会回调该接口
        PushAgent.getInstance(context).register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.i(TAG, "onSuccess: deviceToken:" + deviceToken);
                //注册成功会返回device token
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.i(TAG, "onFailure:" + s + "===========" + s1);
            }
        });
    }

    /**
     * 注册华为离线系统推送
     */
    public void registerHWPush(Context context) {
        HuaWeiRegister.register(context);
    }

    /**
     * 注册小米离线系统推送
     *
     * @param XIAOMI_ID
     * @param XIAOMI_KEY
     */
    public void registerXMPush(Context context, String XIAOMI_ID, String XIAOMI_KEY) {
        MiPushRegistar.register(context, XIAOMI_ID, XIAOMI_KEY);
    }

    /**
     * 注册魅族离线系统推送
     *
     * @param meizuAppId
     * @param meizuAppKey
     */
    public void registerMeizuPush(Context context, String meizuAppId, String meizuAppKey) {
        MeizuRegister.register(context, meizuAppId, meizuAppKey);
    }


    /**
     * 关闭推送
     * @param context
     */
    public void disable(Context context){
        PushAgent.getInstance(context).disable(new IUmengCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }

    /**
     * 打开推送
     * @param context
     */
    public void enable(Context context){
        PushAgent.getInstance(context).enable(new IUmengCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }

    /**
     * 设置push的别名，一般直接用用户名，与设备一一对应，账号退出时需要删除
     *
     * @param userid
     * @param userType
     */
    public void setAlias(Context context, String userid, String userType) {
        PushAgent.getInstance(context).setAlias(userid, userType, (isSuccess, message) -> Log.i(TAG, "isSuccess:" + isSuccess +
                "=============message:" + message));
    }

    public void deleteAlias(Context context, String userid, String userType) {
        PushAgent.getInstance(context).deleteAlias(userid, userType, (isSuccess, message) -> Log.i(TAG, "isSuccess:" + isSuccess +
                "=============message:" + message));
    }


    /**
     * 开发者可自定义用户点击通知栏时的后续动作。自定义行为的数据放在UMessage.custom字段。
     * 若开发者需要处理自定义行为，则可以重写方法dealWithCustomAction()。
     * 其中自定义行为的内容，存放在UMessage.custom中。请在自定义Application类中添加以下代码：
     *
     * 因此若需启动Activity，需为Intent添加Flag：Intent.FLAG_ACTIVITY_NEW_TASK，否则无法启动Activity。
     * @param context
     * @param notificationClickHandler
     */
    public void setNotificationClickHandler(Context context, UmengNotificationClickHandler notificationClickHandler) {
        PushAgent.getInstance(context).setNotificationClickHandler(notificationClickHandler);
    }

    /**
     * 自定义通知栏样式
     * msg.builder_id是服务器下发的消息字段，用来指定通知消息的样式。默认值为0。
     * @param context
     * @param messageHandler
     */
    public void setMessageHandler(Context context, UmengMessageHandler messageHandler) {
        PushAgent.getInstance(context).setMessageHandler(messageHandler);
    }

    /**
     * 通知免打扰模式
     * SDK默认在“23:00”到“7:00
     * 如果需要改变默认的静音时间，如果需要改变默认的静音时间
     * @param context
     * @param startHour
     * @param startMinute
     * @param endHour
     * @param endMinute
     */
    public void setNoDisturbMode(Context context,int startHour, int startMinute, int endHour, int endMinute) {
        PushAgent.getInstance(context).setNoDisturbMode(startHour, startMinute, endHour, endMinute);
    }

    /**
     * 添加标签
     * @param context
     * @param tags
     */
    public void addTags(Context context, String... tags) {
        PushAgent.getInstance(context).getTagManager().addTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final ITagManager.Result result) {
                //isSuccess表示操作是否成功
            }
        },tags);
    }

    /**
     * 删除标签
     * @param context
     * @param tags
     */
    public void deleteTags(Context context, String... tags) {
        PushAgent.getInstance(context).getTagManager().deleteTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final ITagManager.Result result) {
                //isSuccess表示操作是否成功
            }
        },tags);
    }

    /**
     * 获取服务器端的所有标签
     * @param context
     * @param tagListCallBack
     */
    public void getTags(Context context,TagManager.TagListCallBack tagListCallBack) {
        PushAgent.getInstance(context).getTagManager().getTags(tagListCallBack);
    }

    /**
     * 加权标签是给标签增加了一个权值
     * @param context
     * @param hashtable
     */
    public void addWeightedTags(Context context, Hashtable<String, Integer> hashtable) {
        PushAgent.getInstance(context).getTagManager().addWeightedTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final ITagManager.Result result) {
            }
        }, hashtable);
    }

    /**
     * 删除加权标签
     * @param context
     * @param tags
     */
    public void deleteWeightedTags(Context context,String... tags) {
        PushAgent.getInstance(context).getTagManager().deleteWeightedTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final ITagManager.Result result) {
            }
        }, tags);
    }

    /**
     * 获取服务器端的所有加权标签
     * @param context
     * @param weightedTagListCallBack
     */
    public void getWeightedTags(Context context,TagManager.WeightedTagListCallBack weightedTagListCallBack) {
        PushAgent.getInstance(context).getTagManager().getWeightedTags(weightedTagListCallBack);
    }

    /**
     * 用户退出账号的时，删除别名，防止退出还能接收到消息
     *
     * @param userID
     */
    public void onDestroy(Context context, String userID) {
        if (userID == null) {
            return;
        }
        PushAgent.getInstance(context).deleteAlias(userID, "user", (isSuccess, message) -> Log.i(TAG, "isSuccess:" + isSuccess +
                "=============message:" + message));
    }
}