package com.wilinz.yuetingmusic.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User extends LitePalSupport implements Parcelable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!Objects.equals(username, user.username)) return false;
        if (!Objects.equals(password, user.password))
            return false;
        if (!Objects.equals(nickname, user.nickname))
            return false;
        if (!Objects.equals(avatar, user.avatar)) return false;
        return Objects.equals(introduction, user.introduction);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (introduction != null ? introduction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }

    public User() {

    }

    public int id;
    @Column(unique = true, index = true)
    public String username;
    public String password;
    public String nickname;
    public String avatar;
    public String introduction;
    public boolean rememberPassword;
    public List<FavoriteSong> favoriteSongs;
    public List<RecentSong> recentSongs;
    @Column()
    public boolean isActive;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.nickname);
        dest.writeString(this.avatar);
        dest.writeString(this.introduction);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readInt();
        this.username = source.readString();
        this.password = source.readString();
        this.nickname = source.readString();
        this.avatar = source.readString();
        this.introduction = source.readString();
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.username = in.readString();
        this.password = in.readString();
        this.nickname = in.readString();
        this.avatar = in.readString();
        this.introduction = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
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
