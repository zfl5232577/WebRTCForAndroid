package cn.aorise.grid.module.network.entity.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: gaoxu
 * TIME: 2017/8/28
 * Description: This is User
 * Function:人员实体类
 */

public class User implements Parcelable {


    /**
     * id : 2
     * password : 8e4c7c08260b1d0d6f5f0866b85c4ec1
     * imgurl : storage/uploads/20170615/20170615142033DNXwnK.png
     * username : admins
     * status : FREE
     * name : 超级管理员
     * rid : 386939
     * phone : 13632842257
     * mail :
     * isAdmin : 1
     */

    public int id;
    public String password;
    public String imgurl;
    public String username;
    public String status;
    public String name;
    public int rid;
    public String phone;
    public String mail;
    public int isAdmin;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", password='" + password + '\'' +
                ", imgurl='" + imgurl + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", rid=" + rid +
                ", phone='" + phone + '\'' +
                ", mail='" + mail + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.password);
        dest.writeString(this.imgurl);
        dest.writeString(this.username);
        dest.writeString(this.status);
        dest.writeString(this.name);
        dest.writeInt(this.rid);
        dest.writeString(this.phone);
        dest.writeString(this.mail);
        dest.writeInt(this.isAdmin);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.password = in.readString();
        this.imgurl = in.readString();
        this.username = in.readString();
        this.status = in.readString();
        this.name = in.readString();
        this.rid = in.readInt();
        this.phone = in.readString();
        this.mail = in.readString();
        this.isAdmin = in.readInt();
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
