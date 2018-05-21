package cn.aorise.grid.module.network.entity.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: gaoxu
 * TIME: 2017/9/7
 * Description: This is Region
 * Function:当前用户所在辖区的实体类
 */

public class Region implements Parcelable {

    /**
     * id : 386939
     * name : 溆浦县
     * pid : 0
     */

    public int id;
    public String name;
    public int pid;

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pid=" + pid +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.pid);
    }

    public Region() {
    }

    protected Region(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.pid = in.readInt();
    }

    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
        public Region createFromParcel(Parcel source) {
            return new Region(source);
        }

        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
}
