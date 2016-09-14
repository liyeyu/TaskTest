package com.liyeyu.tasktest.binder;

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}
