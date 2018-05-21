package cn.aorise.grid.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.aorise.common.component.common.CmptUtils;
import cn.aorise.common.core.module.network.APICallback;
import cn.aorise.common.core.module.network.APIObserver;
import cn.aorise.common.core.module.network.RxSchedulers;
import cn.aorise.common.core.util.AoriseLog;
import cn.aorise.common.core.util.GsonUtils;
import cn.aorise.common.core.util.SPUtils;
import cn.aorise.common.databinding.AoriseActivityComponentLoginBinding;
import cn.aorise.grid.BuildConfig;
import cn.aorise.grid.R;
import cn.aorise.grid.config.Constant;
import cn.aorise.grid.config.ErrorCodeConstant;
import cn.aorise.grid.interfaces.IRequestHandle;
import cn.aorise.grid.module.cache.UserInfoCache;
import cn.aorise.grid.module.network.GridApiService;
import cn.aorise.grid.module.network.entity.request.ReqLogin;
import cn.aorise.grid.module.network.entity.response.Session;
import cn.aorise.grid.module.network.entity.response.User;
import cn.aorise.grid.ui.base.GridBaseActivity;
import cn.aorise.webrtc.chat.ChatClient;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * 公共登录页面
 * Created by tangjy on 2017/3/17.
 */
public class LoginActivity extends GridBaseActivity implements IRequestHandle<Response<ResponseBody>>, TextWatcher {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private AoriseActivityComponentLoginBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initData() {
        if (SPUtils.getInstance().getBoolean(Constant.SPCache.LOGIN, false)) {
            CmptUtils.gotoTargetActivity(LoginActivity.this, "");
        }
    }

    @Override
    protected void initView() {
        mBinding = DataBindingUtil.setContentView(this, cn.aorise.common.R.layout.aorise_activity_component_login);
        // getToolBar().setNavigationIcon(null);
        String account = SPUtils.getInstance().getString(Constant.SPCache.ACCOUNT, "");
        String password = SPUtils.getInstance().getString(Constant.SPCache.PASSWORD, "");
        if (BuildConfig.MOCK_MODE) {
            account = "system";
            password = "aorise";
        }

        if (!TextUtils.isEmpty(account)) {
            mBinding.etAccount.getEditText().setText(account);
            mBinding.etAccount.getEditText().setSelection(account.length());
        }

        if (!TextUtils.isEmpty(password)) {
            mBinding.etPassword.getEditText().setText(password);
            mBinding.etPassword.getEditText().setSelection(password.length());
        }

        setLoginEnabled(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password));
    }

    @Override
    protected void initEvent() {
        mBinding.etAccount.getEditText().addTextChangedListener(this);
        mBinding.etPassword.getEditText().addTextChangedListener(this);

        mBinding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        // mBinding.etAccount.getEditText().setError(null);
        // mBinding.etPassword.getEditText().setError(null);

        // Store values at the time of the login attempt.
        String account = mBinding.etAccount.getEditText().getText().toString();
        String password = mBinding.etPassword.getEditText().getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            // mBinding.etPassword.getEditText().setError(getString(R.string.aorise_component_error_field_required));
            showToast(R.string.aorise_component_error_field_required);
            focusView = mBinding.etPassword.getEditText();
            cancel = true;
        } else if (!isPasswordValid(password)) {
            // mBinding.etPassword.getEditText().setError(getString(R.string.aorise_component_error_invalid_password));
            showToast(R.string.aorise_component_error_invalid_password);
            focusView = mBinding.etPassword.getEditText();
            cancel = true;
        }

        // Check for a valid account address.
        if (TextUtils.isEmpty(account)) {
            // mBinding.etAccount.getEditText().setError(getString(R.string.aorise_component_error_field_required));
            showToast(R.string.aorise_component_error_invalid_password);
            focusView = mBinding.etAccount;
            cancel = true;
        } else if (!isAccountValid(account)) {
            // mBinding.etAccount.getEditText().setError(getString(R.string.aorise_component_error_invalid_account));
            showToast(R.string.aorise_component_error_invalid_account);
            focusView = mBinding.etAccount;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            request(new ReqLogin(account, password));
        }
    }

    private void saveLoginInfo(String account, String password) {
        SPUtils.getInstance().put(Constant.SPCache.ACCOUNT, account);
        SPUtils.getInstance().put(Constant.SPCache.PASSWORD, password);
    }

    private void removeLoginInfo() {
        SPUtils.getInstance().remove(Constant.SPCache.ACCOUNT);
        SPUtils.getInstance().remove(Constant.SPCache.PASSWORD);
    }

    private boolean isAccountValid(String account) {
        //TODO: Replace this with your own logic
        return account.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    private void request(ReqLogin reqLogin) {
        AoriseLog.i(TAG, "request = " + reqLogin.toJson());

        GridApiService.Factory.create().login(reqLogin.getUsername(), reqLogin.getPassword())
                .compose(RxSchedulers.compose(this, this.<Response<ResponseBody>>bindToLifecycle()))
                .subscribe(new APIObserver<Response<ResponseBody>>(this, new APICallback<Response<ResponseBody>>() {
                    @Override
                    public void onError(Throwable throwable) {
                        AoriseLog.i(TAG, "===========> onError");
                        handleError(throwable);
                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        AoriseLog.i(TAG, "===========> onNext");
                        handleSuccess(responseBodyResponse);
                    }
                }));
    }

    private void getUserInfo() {
        GridApiService.Factory.create().getSession()
                .compose(RxSchedulers.compose(this, this.<Session>bindToLifecycle()))
                .subscribe(new APIObserver<Session>(this, new APICallback<Session>() {
                    @Override
                    public void onError(Throwable throwable) {
                        showToast(getString(R.string.grid_login_user_info_failed));
                        AoriseLog.d(TAG, "===========> onError");
                    }

                    @Override
                    public void onNext(Session sessionResonse) {
                        AoriseLog.i(TAG, "===========> onNext");
                        if (sessionResonse == null) {
                            return;
                        }
                        SPUtils.getInstance().put(Constant.SPCache.USER, GsonUtils.toJson(sessionResonse));
                        SPUtils.getInstance().put(Constant.SPCache.LOGIN, true);
                        CmptUtils.gotoTargetActivity(LoginActivity.this, "");
                        User user = sessionResonse.getUser();
                        ChatClient.getInstance().login(user.username,user.name,user.imgurl);
                        EventBus.getDefault().post(true);
                    }
                }));
    }

    @Override
    public void handleError(Throwable e) {
        e.printStackTrace();
        showToast(R.string.grid_network_fail);
    }

    @Override
    public void handleSuccess(Response<ResponseBody> responseBodyResponse) {
        // 账号密码处理
        if (mBinding.cbPassword.isChecked()) {
            saveLoginInfo(mBinding.etAccount.getEditText().getText().toString(),
                    mBinding.etPassword.getEditText().getText().toString());
        } else {
            removeLoginInfo();
        }

        // 返回数据处理
        AoriseLog.i(TAG, "===========> code: " + responseBodyResponse.code());
        AoriseLog.i(TAG, "===========> headers: " + responseBodyResponse.headers().toString());

        //将cookie值进行存储
        Headers headers = responseBodyResponse.headers();
        List<String> cookies = headers.values("Set-Cookie");
        if (cookies.size() != 0) {
            String session = cookies.get(0);
            String s = session.substring(0, session.indexOf(";"));
            UserInfoCache.saveSetCookie(s);
        }

        if (null != responseBodyResponse.body()) {
            AoriseLog.i(TAG, "===========> body = " + responseBodyResponse.body().toString());
        }

        if (ErrorCodeConstant.LoginErrorCode.SUCCESS == responseBodyResponse.code()) {
            getUserInfo();
        } else {
            showToast(ErrorCodeConstant.LoginHint.get(
                    responseBodyResponse.code(), ErrorCodeConstant.COMMON_FAIL));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean isAccountEmpty = TextUtils.isEmpty(mBinding.etAccount.getEditText().getText().toString());
        boolean isPasswordEmpty = TextUtils.isEmpty(mBinding.etPassword.getEditText().getText().toString());
        AoriseLog.i(TAG, "isAccountEmpty: " + isAccountEmpty + " ;isPasswordEmpty: " + isPasswordEmpty);
        setLoginEnabled(!isAccountEmpty && !isPasswordEmpty);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 登录按钮是否可以生效
     *
     * @param enabled true生效 false不生效
     */
    private void setLoginEnabled(boolean enabled) {
        AoriseLog.i(TAG, "enabled: " + enabled);
        mBinding.btnSignIn.setEnabled(enabled);
    }
}
