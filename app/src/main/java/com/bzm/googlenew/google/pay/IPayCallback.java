package com.bzm.googlenew.google.pay;

/**
 * Created by bzm0518
 * on 2021/1/12
 */
public interface IPayCallback<T> {

    void success(T t);

    void fail(String msg);


}
