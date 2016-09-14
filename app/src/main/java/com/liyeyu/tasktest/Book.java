package com.liyeyu.tasktest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Liyeyu on 2016/9/10.
 */
public class Book implements Parcelable {

    public int bookId;
    public String name;

    public Book(int bookId) {
        this.bookId = bookId;
    }

    public Book(int bookId, String name) {
        this.bookId = bookId;
        this.name = name;
    }

    private Book(Parcel in) {
        bookId = in.readInt();
        name = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(bookId);
        parcel.writeString(name);
    }

}
