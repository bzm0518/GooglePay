package com.bzm.googlenew.google.sign_in;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;

/**
 * Created by bzm0518
 * on 2020/8/7
 * 参考国内google网站
 * https://developers.google.cn/identity/sign-in/android
 * demo:https://github.com/googlesamples/google-services.git
 */
public class GoogleSignInHelper {
    //google后台的 CLIENT_ID
    private String CLIENT_ID = "102452211111-ucpuk5kv3ue5dgu39a19mpuk02aefit6.apps.googleusercontent.com";

    private GoogleSignInClient mClient ;
    /**
     * //在登录活动的onCreate方法中，配置Google登录以请求您的应用所需的用户数据。
     // 例如，要配置Google登录以请求用户的ID和基本配置文件信息，请使用DEFAULT_SIGN_IN参数创建GoogleSignInOptions对象。
     // 要同时请求用户的电子邮件地址，请使用requestEmail选项创建GoogleSignInOptions对象。
     requestIdToken() 添加这个方法，后面获取的时候不会为空
     */
    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(CLIENT_ID)
            .build();

    private Activity mCurrentActivity;

    public GoogleSignInHelper(Activity activity){
        mCurrentActivity = activity;
        mClient = GoogleSignIn.getClient(activity,gso);

    }

    /**
     * google登录
     */
    public void googleSignIn(){
        //如果用户已登录，请检查现有的Google登录帐户
        // GoogleSignInAccount将为非null。
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mCurrentActivity);

        Intent signInIntent = mClient.getSignInIntent();
        mCurrentActivity.startActivityForResult(signInIntent, SIGN_IN);
    }


    /**
     * google登录回调
     * 需要Activity中调用这个 onActivityResult 方法
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        //检查现有的Google登录帐户，如果用户已在GoogleSignInAccount中登录，则该帐户将为非空。
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.w("$Msg google login","Token :" + account.getIdToken()
                    + "， Email : " + account.getEmail() + ",id : "+ account.getId());

        } catch (ApiException e) {
            e.printStackTrace();
        }

    }


}
