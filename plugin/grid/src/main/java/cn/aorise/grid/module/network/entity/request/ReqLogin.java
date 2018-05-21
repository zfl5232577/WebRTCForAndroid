package cn.aorise.grid.module.network.entity.request;

import cn.aorise.grid.config.Constant;

/**
 * Created by tangjy on 2017/9/5.
 */
public class ReqLogin extends Request {
    private int type;
    private String username;
    private String password;

    public ReqLogin() {
    }

    public ReqLogin(int type, String username, String password) {
        this.type = type;
        this.username = username;
        this.password = password;
    }

    public ReqLogin(String username, String password) {
        this(Constant.LoginType.MOBILE, username, password);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
