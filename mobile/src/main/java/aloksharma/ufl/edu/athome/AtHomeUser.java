package aloksharma.ufl.edu.athome;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alok on 3/13/2015.
 */
public class AtHomeUser implements Parcelable{
    private String first_name;
    private String last_name;
    private String email;
    private List<String> friendList;
    private Boolean status;
    private String wifi;

    public void setFirstName(String first_name){
        this.first_name = first_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public String getWifi() {
        return wifi;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setFriendList(List<String> friendList) {
        this.friendList = friendList;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getFriendList() {
        return friendList;
    }

    public boolean getStatus(){
        return status;
    }


    public AtHomeUser(){

    }

    protected AtHomeUser(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        email = in.readString();
        if (in.readByte() == 0x01) {
            friendList = new ArrayList<String>();
            in.readList(friendList, String.class.getClassLoader());
        } else {
            friendList = null;
        }
        byte statusVal = in.readByte();
        status = statusVal == 0x02 ? null : statusVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(email);
        if (friendList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(friendList);
        }
        if (status == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (status ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AtHomeUser> CREATOR = new Parcelable.Creator<AtHomeUser>() {
        @Override
        public AtHomeUser createFromParcel(Parcel in) {
            return new AtHomeUser(in);
        }

        @Override
        public AtHomeUser[] newArray(int size) {
            return new AtHomeUser[size];
        }
    };
}
