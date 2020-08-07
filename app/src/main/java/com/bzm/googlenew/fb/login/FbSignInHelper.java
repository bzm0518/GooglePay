package com.bzm.googlenew.fb.login;

import android.app.Activity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

/**
 * Created by bzm0518
 * on 2020/8/7
 */
public class FbSignInHelper {

    /**
     * Facebook登录的CallbackManager
     */
    private CallbackManager mCallbackManager = CallbackManager.Factory.create();

    public FbSignInHelper(){
    }

    /**
     * Facebook登录
     * @param activity
     */
    public void fbLogin(Activity activity){

        //是否登录过Facebook
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            //登录过Facebook先登出
            LoginManager.getInstance().logOut();
        }

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Facebook登录成功后,通过AccessToken获取token_for_business

            }

            @Override
            public void onCancel() {
                //登录取消
            }

            @Override
            public void onError(FacebookException error) {
                //登录错误
            }
        });

        //发起Facebook登录
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));

    }



}
