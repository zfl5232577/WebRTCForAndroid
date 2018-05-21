package cn.aorise.grid.module.network.entity.response;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import cn.aorise.grid.module.network.entity.request.Request;

/**
 * Author: gaoxu
 * TIME: 2017/8/28
 * Description: This is Popedom
 * Function:
 */

public class Popedom implements Parcelable {
    private String name;
    private int childPopedom;
    private List<User> mUsers;

    public Popedom(String name) {
        this.name = name;
    }

    public Popedom(String name, int childPopedom, List<User> users) {
        this.name = name;
        this.childPopedom = childPopedom;
        mUsers = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChildPopedom() {
        return childPopedom;
    }

    public void setChildPopedom(int childPopedom) {
        this.childPopedom = childPopedom;
    }

    public List<User> getUsers() {
        return mUsers;
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.childPopedom);
        dest.writeList(this.mUsers);
    }

    protected Popedom(Parcel in) {
        this.name = in.readString();
        this.childPopedom = in.readInt();
        this.mUsers = new ArrayList<User>();
        in.readList(this.mUsers, User.class.getClassLoader());
    }

    public static final Parcelable.Creator<Popedom> CREATOR = new Parcelable.Creator<Popedom>() {
        @Override
        public Popedom createFromParcel(Parcel source) {
            return new Popedom(source);
        }

        @Override
        public Popedom[] newArray(int size) {
            return new Popedom[size];
        }
    };
}
