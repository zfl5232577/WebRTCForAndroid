package cn.aorise.webrtc.chat;

import cn.aorise.webrtc.ui.PushActivity;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/09/21
 *     desc   : TODO
 *     version: 1.0
 * </pre>
 */
public interface OnPushCallback {
    void handPushMessage(PushActivity.PushMessageBean message);
}
