package cn.aorise.webrtc.api;

import cn.aorise.webrtc.chat.User;

/**
 * Created by tangjy on 2015/11/3 0003.
 */
public class Constant {

    public static Boolean hasNetWork = true;

    public static class EventMessage {
        //登录冲突
        public static final String EVENTBUS_LOGIN_CONFLICT = "login_conflict";

        //activity关闭 fragment自动刷新开启
        public static final String EVENTBUS_ISREFRESH = "refresh";

        //进入MainActivity刷新状态
        public static final String EVENTBUS_REFRESH_STATUS = "refresh_status";

        public static final String EVENTBUS_SWITCH_AUDIO_STATUS = "switch_audio_status";
    }

    public static class Status {
        public static final String ON_LINE = "FREE";
        public static final String OFF_Line = "AWAY";
        public static final String BUSY = "BUSY";
    }

    public static class DeviceType {
        public static final String PC = "PC";
        public static final String MOBILE = "MOBILE";
    }

    public static class SignalType {

        public static final String SIGNAL_OFFERED = "OFFERED";
        public static final String SIGNAL_ANSWERED = "ANSWERED";
        public static final String SIGNAL_CANDIDATE = "CANDIDATE";
        public static final String SIGNAL_REFUSED = "REFUSED";
        public static final String SIGNAL_MEMBER_REFUSED = "MEMBER_REFUSED";
        public static final String SIGNAL_REMOVED = "REMOVED";
        public static final String SIGNAL_ROOM_DISSOLVED = "ROOM_DISSOLVED";
        public static final String SIGNAL_MEMBER_LEAVED = "MEMBER_LEAVED";
        public static final String SIGNAL_DUPLICATE_CONNECTION = "DUPLICATE_CONNECTION";
        public static final String SIGNAL_PUSH = "PUSH";

    }

    public static class SignalDestination {
        public static final String SIGNAL_Destination_OFFERE = "/signal/offer";
        public static final String SIGNAL_Destination_ANSWER = "/signal/answer";
        public static final String SIGNAL_Destination_CANDIDATE = "/signal/candidate";
        public static final String SIGNAL_Destination_REFUSE = "/signal/refuse";
        public static final String SIGNAL_Destination_REMOVE = "/signal/remove";
        public static final String SIGNAL_Destination_LEAVE = "/signal/leave";
        public static final String SIGNAL_Destination_PONG = "/signal/pong";
        public static final String SIGNAL_Destination_PUSH = "/signal/push";
    }

    public static class SPCache {
        public static final String USER = "user";
        public static final String ACCOUNT = "account";
        public static final String PASSWORD = "password";
        public static final String LOGIN = "login";
    }

    public static class LoginInfo {
        public static boolean isLogin = false;
        public static User user;
    }

    /**
     * 友盟别名类型
     */
    public static class ALIAS_TYPE {
        public static final String USER = "user";
    }
}
