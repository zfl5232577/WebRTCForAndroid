package cn.aorise.webrtc.chat;

import cn.aorise.webrtc.BuildConfig;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/28
 *     desc   : 框架配置参数
 *     version: 1.0
 * </pre>
 */
public class ChatAPIConfig {
    private String SIGNAL_URL;
    private String TOKEN;
    private String STUN_URL;
    private String TURN_URL;
    private String TURN_ACCOUNT;
    private String TURN_PASSWORD;
    private String UMENG_APPKEY;
    private String UMENG_SECRET;
    private String XIAOMI_ID;
    private String XIAOMI_KEY;

    /**
     * @param SIGNAL_URL    信令服务及WebSocket服务地址
     * @param TOKEN         组件服务器分配token
     * @param STUN_URL      stun服务地址
     * @param TURN_URL      turn服务地址
     * @param TURN_ACCOUNT  turn服务账号
     * @param TURN_PASSWORD turn服务密码
     * @param UMENG_APPKEY  友盟push APPKEY
     * @param UMENG_SECRET  友盟push SECRET
     * @param XIAOMI_ID     小米push id
     * @param XIAOMI_KEY    小米push KEY
     */
    public ChatAPIConfig(String SIGNAL_URL, String TOKEN, String STUN_URL, String TURN_URL, String TURN_ACCOUNT, String TURN_PASSWORD, String UMENG_APPKEY, String UMENG_SECRET, String XIAOMI_ID, String XIAOMI_KEY) {
        this.SIGNAL_URL = SIGNAL_URL;
        this.TOKEN = TOKEN;
        this.STUN_URL = STUN_URL;
        this.TURN_URL = TURN_URL;
        this.TURN_ACCOUNT = TURN_ACCOUNT;
        this.TURN_PASSWORD = TURN_PASSWORD;
        this.UMENG_APPKEY = UMENG_APPKEY;
        this.UMENG_SECRET = UMENG_SECRET;
        this.XIAOMI_ID = XIAOMI_ID;
        this.XIAOMI_KEY = XIAOMI_KEY;
    }

    public String getSIGNAL_URL() {
        return SIGNAL_URL;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public String getSTUN_URL() {
        return STUN_URL;
    }

    public String getTURN_URL() {
        return TURN_URL;
    }

    public String getTURN_ACCOUNT() {
        return TURN_ACCOUNT;
    }

    public String getTURN_PASSWORD() {
        return TURN_PASSWORD;
    }

    public String getUMENG_APPKEY() {
        return UMENG_APPKEY;
    }

    public String getUMENG_SECRET() {
        return UMENG_SECRET;
    }

    public String getXIAOMI_ID() {
        return XIAOMI_ID;
    }

    public String getXIAOMI_KEY() {
        return XIAOMI_KEY;
    }
}
