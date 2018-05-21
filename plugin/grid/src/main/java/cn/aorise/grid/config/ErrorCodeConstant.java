package cn.aorise.grid.config;

import android.util.SparseIntArray;

import cn.aorise.grid.R;

/**
 * Created by tangjy on 2017/9/5.
 */
public class ErrorCodeConstant {
    /**
     * 错误码默认数组大小
     */
    private static final int SIZE = 5;

    /**
     * 不在错误码范围里面的通用提示
     */
    public static final int COMMON_FAIL = R.string.grid_error_code_common_fail;

    public static class ErrorCode {
        // 公共的成功状态
        public static final int SUCCESS = 200;
    }

    /**
     * 登录错误码
     */
    public static class LoginErrorCode extends ErrorCode {
        public static final int USER_NOT_EXIST = 400;
        public static final int WRONG_PASSWORD = 401;
        public static final int DISABLED = 403;
    }

    public static final SparseIntArray LoginHint = new SparseIntArray(SIZE);


    static {
        LoginHint.put(LoginErrorCode.USER_NOT_EXIST, R.string.grid_error_code_login_user_not_exist);
        LoginHint.put(LoginErrorCode.WRONG_PASSWORD, R.string.grid_error_code_login_wrong_password);
        LoginHint.put(LoginErrorCode.DISABLED, R.string.grid_error_code_login_disabled);
    }
}
