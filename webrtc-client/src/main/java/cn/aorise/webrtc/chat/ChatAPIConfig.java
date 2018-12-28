package cn.aorise.webrtc.chat;

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
    private ConfigParams mConfigParams;

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
        this.mConfigParams = new ConfigParams();
        this.mConfigParams.setSIGNAL_URL(SIGNAL_URL);
        this.mConfigParams.setTOKEN(TOKEN);
        this.mConfigParams.setSTUN_URL(STUN_URL);
        this.mConfigParams.setTURN_URL(TURN_URL);
        this.mConfigParams.setTURN_ACCOUNT(TURN_ACCOUNT);
        this.mConfigParams.setTURN_PASSWORD(TURN_PASSWORD);
        this.mConfigParams.setUMENG_APPKEY(UMENG_APPKEY);
        this.mConfigParams.setUMENG_SECRET(UMENG_SECRET);
        this.mConfigParams.setXIAOMI_ID(XIAOMI_ID);
        this.mConfigParams.setXIAOMI_KEY(XIAOMI_KEY);
    }

    public ChatAPIConfig(ConfigParams configParams){
        this.mConfigParams = configParams;
    }

    public ConfigParams getConfigParams() {
        return mConfigParams;
    }

    public static class Builder{
        private ConfigParams mConfigParams;

        public Builder() {
            mConfigParams = new ConfigParams();
        }

        public Builder signalUrl(String SIGNAL_URL){
            this.mConfigParams.setSIGNAL_URL(SIGNAL_URL);
            return this;
        }
        public Builder token(String TOKEN){
            this.mConfigParams.setTOKEN(TOKEN);
            return this;
        }
        public Builder stunUrl(String STUN_URL){
            this.mConfigParams.setSTUN_URL(STUN_URL);
            return this;
        }
        public Builder turnUrl(String TURN_URL){
            this.mConfigParams.setTURN_URL(TURN_URL);
            return this;
        }
        public Builder turnAccountAndPassword(String TURN_ACCOUNT,String TURN_PASSWORD){
            this.mConfigParams.setTURN_ACCOUNT(TURN_ACCOUNT);
            this.mConfigParams.setTURN_PASSWORD(TURN_PASSWORD);
            return this;
        }

        public Builder umengPush(String UMENG_APPKEY,String UMENG_SECRET){
            this.mConfigParams.setUMENG_APPKEY(UMENG_APPKEY);
            this.mConfigParams.setUMENG_SECRET(UMENG_SECRET);
            return this;
        }

        public Builder xiaomiPush(String XIAOMI_ID,String XIAOMI_KEY){
            this.mConfigParams.setXIAOMI_ID(XIAOMI_ID);
            this.mConfigParams.setXIAOMI_KEY(XIAOMI_KEY);
            return this;
        }

        public Builder meizuPush(String meizuAppId,String meizuAppKey){
            this.mConfigParams.setMEIZUAPP_ID(meizuAppId);
            this.mConfigParams.setMEIZUAPP_KEY(meizuAppKey);
            return this;
        }

        public ChatAPIConfig build(){
            return new ChatAPIConfig(mConfigParams);
        }
    }
}
