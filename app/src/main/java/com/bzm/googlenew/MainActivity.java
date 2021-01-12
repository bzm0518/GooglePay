package com.bzm.googlenew;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.android.billingclient.api.Purchase;
import com.bzm.googlenew.google.pay.GooglePayHelper;
import com.bzm.googlenew.google.pay.IPayCallback;
import com.bzm.googlenew.google.pay.PayInfo;

public class MainActivity extends AppCompatActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void googlePay(View view) {
        PayInfo info = new PayInfo();
        info.setMoney("99.9");
        info.setOrderId("123456");
        info.setProductId("com.bzm.google_99.9");
        GooglePayHelper.getInstance().startGooglePay(this, info, new IPayCallback<Purchase>() {
            @Override
            public void success(Purchase purchase) {
                //purchase.getPurchaseToken()  purchase.getOriginalJson() 服务端需要验证
                //purchase.getOrderId() Google订单
                /**
                 * purchase.getOriginalJson()
                 * purchase.getDeveloperPayload()透传字段  已废弃这个方法了，返回值为空的,需要透传可以在调用支付
                 * BillingFlowParams 的时候 设置 setObfuscatedAccountId setObfuscatedProfileId
                 */

            }

            @Override
            public void fail(String msg) {

            }
        });
    }
}