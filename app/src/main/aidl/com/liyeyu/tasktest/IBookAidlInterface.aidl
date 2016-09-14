// IBookAidlInterface.aidl
package com.liyeyu.tasktest;
import com.liyeyu.tasktest.Book;
import com.liyeyu.tasktest.INewBookListener;

// Declare any non-default types here with import statements

interface IBookAidlInterface {
    //in 输入参数 inout ,out
    void add(in Book book);

    List<Book> getBookList();

    void registerListener(INewBookListener listener);

    void unregisterListener(INewBookListener listener);
}
