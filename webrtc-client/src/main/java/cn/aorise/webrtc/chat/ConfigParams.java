package cn.aorise.webrtc.chat;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/05/27
 *     desc   : 配置参数
 *     version: 1.0
 * </pre>
 */
public class ConfigParams {
    /**
     *  SIGNAL_URL    信令服务及WebSocket服务地址
     *  TOKEN         组件服务器分配token
     *  STUN_URL      stun服务地址
     *  TURN_URL      turn服务地址
     *  TURN_ACCOUNT  turn服务账号
     *  TURN_PASSWORD turn服务密码
     *  UMENG_APPKEY  友盟push APPKEY
     *  UMENG_SECRET  友盟push SECRET
     *  XIAOMI_ID     小米push id
     *  XIAOMI_KEY    小米push KEY
     */
    private String SIGNAL_URL="";
    private String TOKEN="";
    private String STUN_URL="";
    private String TURN_URL="";
    private String TURN_ACCOUNT="";
    private String TURN_PASSWORD="";
    private String UMENG_APPKEY="";
    private String UMENG_SECRET="";
    private String XIAOMI_ID="";
    private String XIAOMI_KEY="";
    private String MEIZUAPP_ID="";
    private String MEIZUAPP_KEY="";

    public String getSIGNAL_URL() {
        return SIGNAL_URL;
    }

    public void setSIGNAL_URL(String SIGNAL_URL) {
        this.SIGNAL_URL = SIGNAL_URL;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public String getSTUN_URL() {
        return STUN_URL;
    }

    public void setSTUN_URL(String STUN_URL) {
        this.STUN_URL = STUN_URL;
    }

    public String getTURN_URL() {
        return TURN_URL;
    }

    public void setTURN_URL(String TURN_URL) {
        this.TURN_URL = TURN_URL;
    }

    public String getTURN_ACCOUNT() {
        return TURN_ACCOUNT;
    }

    public void setTURN_ACCOUNT(String TURN_ACCOUNT) {
        this.TURN_ACCOUNT = TURN_ACCOUNT;
    }

    public String getTURN_PASSWORD() {
        return TURN_PASSWORD;
    }

    public void setTURN_PASSWORD(String TURN_PASSWORD) {
        this.TURN_PASSWORD = TURN_PASSWORD;
    }

    public String getUMENG_APPKEY() {
        return UMENG_APPKEY;
    }

    public void setUMENG_APPKEY(String UMENG_APPKEY) {
        this.UMENG_APPKEY = UMENG_APPKEY;
    }

    public String getUMENG_SECRET() {
        return UMENG_SECRET;
    }

    public void setUMENG_SECRET(String UMENG_SECRET) {
        this.UMENG_SECRET = UMENG_SECRET;
    }

    public String getXIAOMI_ID() {
        return XIAOMI_ID;
    }

    public void setXIAOMI_ID(String XIAOMI_ID) {
        this.XIAOMI_ID = XIAOMI_ID;
    }

    public String getXIAOMI_KEY() {
        return XIAOMI_KEY;
    }

    public void setXIAOMI_KEY(String XIAOMI_KEY) {
        this.XIAOMI_KEY = XIAOMI_KEY;
    }

    public String getMEIZUAPP_ID() {
        return MEIZUAPP_ID;
    }

    public void setMEIZUAPP_ID(String MEIZUAPP_ID) {
        this.MEIZUAPP_ID = MEIZUAPP_ID;
    }

    public String getMEIZUAPP_KEY() {
        return MEIZUAPP_KEY;
    }

    public void setMEIZUAPP_KEY(String MEIZUAPP_KEY) {
        this.MEIZUAPP_KEY = MEIZUAPP_KEY;
    }
}
