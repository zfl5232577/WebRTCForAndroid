package cn.aorise.push;

import android.content.Context;
import android.util.Log;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/21
 *     desc   : TODO
 *     version: 1.0
 * </pre>
 */
public class PushHelper {
    private static final String TAG = PushHelper.class.getSimpleName();
    private Context mContext;
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
     * @param context
     * @param pushSecret 友盟pushSecret
     */
    public void init(Context context, String pushSecret,String XIAOMI_ID,String XIAOMI_KEY){
        this.mContext = context;
        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(mContext, UMConfigure.DEVICE_TYPE_PHONE,
                pushSecret);
        register();
        registerHWPush();
        registerXMPush(XIAOMI_ID,XIAOMI_KEY);
    }

    /**
     * 初始化友盟推送
     * @param context
     * @param appkey  友盟AppKey
     * @param channel 推送类型
     * @param pushSecret 友盟pushSecret
     */
    public void init(Context context, String appkey, String channel,String pushSecret,String XIAOMI_ID,String XIAOMI_KEY){
        this.mContext = context;
        //初始化组件化基础库, 统计SDK/推送SDK/分享SDK都必须调用此初始化接口
        UMConfigure.init(mContext, appkey,channel,UMConfigure.DEVICE_TYPE_PHONE,
                pushSecret);
        register();
        registerHWPush();
        registerXMPush(XIAOMI_ID,XIAOMI_KEY);
    }

    /**
     * 设置日志开关
     * @param enabled
     */
    public void setLogEnabled(boolean enabled){
        UMConfigure.setLogEnabled(enabled);
    }

    /**
     * PushSDK初始化(如使用推送SDK，必须调用此方法
     */
    private void register(){
        //注册推送服务，每次调用register方法都会回调该接口
        PushAgent.getInstance(mContext).register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.i(TAG, "onSuccess: deviceToken:"+deviceToken );
                //注册成功会返回device token
            }
            @Override
            public void onFailure(String s, String s1) {
                Log.i(TAG, "onFailure:"+s+"==========="+s1 );
            }
        });
    }

    /**
     * 注册华为离线系统推送
     */
    private void registerHWPush(){
        HuaWeiRegister.register(mContext);
    }

    /**
     * 注册小米离线系统推送
     * @param XIAOMI_ID
     * @param XIAOMI_KEY
     */
    private void registerXMPush(String XIAOMI_ID,String XIAOMI_KEY){
        MiPushRegistar.register(mContext, XIAOMI_ID, XIAOMI_KEY);
    }

    /**
     * 设置push的别名，一般直接用用户名，与设备一一对应，账号退出时需要删除
     * @param userid
     * @param userType
     */
    public void setAlias(String userid,String userType){
        PushAgent.getInstance(mContext).setAlias(userid,userType,(isSuccess, message) -> Log.i(TAG,"isSuccess:"+isSuccess+
                "=============message:"+message));
    }

    /**
     * 用户退出账号的时，删除别名，防止退出还能接收到消息
     * @param userID
     */
    public void onDestroy(String userID,String userType){
        if (userID==null){
            return;
        }
        PushAgent.getInstance(mContext).deleteAlias(userID, userType,(isSuccess, message) -> Log.i(TAG,"isSuccess:"+isSuccess+
                "=============message:"+message));
    }
}