package cn.aorise.webrtc.chat;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/05/15
 *     desc   : 冲突登陆回调接口
 *     version: 1.0
 * </pre>
 */
public interface OnDuplicateConnectionCallback {
    void onExit();
    void onReLogin();
}
