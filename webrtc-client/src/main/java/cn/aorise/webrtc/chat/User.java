package cn.aorise.webrtc.chat;

import android.os.Parcel;
import android.os.Parcelable;

import cn.aorise.webrtc.api.Constant;

/**
 * Author: gaoxu
 * TIME: 2017/8/28
 * Description: This is User
 * Function:人员实体类
 */

public class User implements Parcelable {

    private String userIcon;
    private String userName;
    private String nickName;
    public String status = Constant.Status.ON_LINE;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userIcon);
        dest.writeString(this.userName);
        dest.writeString(this.nickName);
        dest.writeString(this.status);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.userIcon = in.readString();
        this.userName = in.readString();
        this.nickName = in.readString();
        this.status = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
