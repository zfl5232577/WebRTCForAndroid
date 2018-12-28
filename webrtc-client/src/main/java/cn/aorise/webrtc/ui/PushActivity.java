package cn.aorise.webrtc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;

import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.webrtc.chat.ChatClient;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/04/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PushActivity extends UmengNotifyClickActivity {
    private static String TAG = PushActivity.class.getName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onMessage(Intent intent) {
        super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
        String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        Log.i(TAG, body);
        PushMessageBean bean = GsonUtils.fromJson(body, PushMessageBean.class);
        if (bean != null && bean.getExtra() != null) {
            if (ChatClient.getInstance().getOnPushCallback() == null) {
                Intent intent1 = BaseCallActivity.getIntent(getApplicationContext(),
                        ChatClient.getInstance().getCallActivity(),
                        bean.getExtra().getUsername(), bean.getExtra().getImgurl(),
                        bean.getExtra().getName(), BaseCallActivity.TYPE_INVITING,
                        true, !bean.getExtra().isAudioFlag(),
                        bean.getExtra().getCreateTime(),bean.getExtra().getExtras());
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            } else {
                ChatClient.getInstance().getOnPushCallback().handPushMessage(bean);
            }
        }
        finish();
    }

    public static class PushMessageBean {

        /**
         * display_type : notification
         * extra : {"imgurl":"imgurl","name":"name","username":"username","createtime":132321321312,"extras":"{}"}
         * msg_id : umerfub152403662362410
         * body : {"after_open":"go_app","play_lights":"false","ticker":"离线1530","play_vibrate":"false","text":"离线1530离线1530离线1530离线1530离线1530离线1530","title":"离线1530","play_sound":"true"}
         * random_min : 0
         */

        private String display_type;
        private ExtraBean extra;
        private String msg_id;
        private BodyBean body;
        private int random_min;

        public String getDisplay_type() {
            return display_type;
        }

        public void setDisplay_type(String display_type) {
            this.display_type = display_type;
        }

        public ExtraBean getExtra() {
            return extra;
        }

        public void setExtra(ExtraBean extra) {
            this.extra = extra;
        }

        public String getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(String msg_id) {
            this.msg_id = msg_id;
        }

        public BodyBean getBody() {
            return body;
        }

        public void setBody(BodyBean body) {
            this.body = body;
        }

        public int getRandom_min() {
            return random_min;
        }

        public void setRandom_min(int random_min) {
            this.random_min = random_min;
        }

        public static class ExtraBean {
            /**
             * imgurl : imgurl
             * name : name
             * username : username
             */

            private String imgurl;
            private String name;
            private String username;
            private boolean audioFlag;
            private long createTime;
            private String extras;

            public String getExtras() {
                return extras;
            }

            public void setExtras(String extras) {
                this.extras = extras;
            }

            public boolean isAudioFlag() {
                return audioFlag;
            }

            public void setAudioFlag(boolean audioFlag) {
                this.audioFlag = audioFlag;
            }

            public long getCreateTime() {
                return createTime;
            }

            public void setCreateTime(long createTime) {
                this.createTime = createTime;
            }

            public String getImgurl() {
                return imgurl;
            }

            public void setImgurl(String imgurl) {
                this.imgurl = imgurl;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }

        public static class BodyBean {
            /**
             * after_open : go_app
             * play_lights : false
             * ticker : 离线1530
             * play_vibrate : false
             * text : 离线1530离线1530离线1530离线1530离线1530离线1530
             * title : 离线1530
             * play_sound : true
             */

            private String after_open;
            private String play_lights;
            private String ticker;
            private String play_vibrate;
            private String text;
            private String title;
            private String play_sound;

            public String getAfter_open() {
                return after_open;
            }

            public void setAfter_open(String after_open) {
                this.after_open = after_open;
            }

            public String getPlay_lights() {
                return play_lights;
            }

            public void setPlay_lights(String play_lights) {
                this.play_lights = play_lights;
            }

            public String getTicker() {
                return ticker;
            }

            public void setTicker(String ticker) {
                this.ticker = ticker;
            }

            public String getPlay_vibrate() {
                return play_vibrate;
            }

            public void setPlay_vibrate(String play_vibrate) {
                this.play_vibrate = play_vibrate;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getPlay_sound() {
                return play_sound;
            }

            public void setPlay_sound(String play_sound) {
                this.play_sound = play_sound;
            }
        }
    }
}
