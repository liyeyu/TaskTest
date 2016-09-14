// INewBookListener.aidl
package com.liyeyu.tasktest;
import com.liyeyu.tasktest.Book;

interface INewBookListener {
    void onAddNewBook(in Book book);
}
