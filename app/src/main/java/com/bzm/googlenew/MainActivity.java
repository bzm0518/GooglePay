package com.bzm.googlenew;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }



    private void initialize(){
        //初始化BillingClient
        billingClient = BillingClient.newBuilder(this)
                //监听支付
                .setListener(this)
                .enablePendingPurchases()
                .build();
        //建立与Google Play的连接
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // 已连接到google，可以支付或查询
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    /**
     * 调起支付
     * @throws JSONException
     */
    private void pay() throws JSONException {
        //sku 唯一商品 ID（也称为 SKU）。必须以小写字母或数字开头，并且只能由小写字母 (a-z)、数字 (0-9)、下划线 (_) 和句点 (.) 组成
        //可包名_金额 这种格式 com.bzm.google_0.99
        String skuToSell = "premium_upgrade";
        List<String> skuList = new ArrayList<>();
        skuList.add(skuToSell);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            // SkuDetails object obtained above.
                            SkuDetails skuDetails = skuDetailsList.get(0);

                            BillingFlowParams purchaseParams =
                                    BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build();

                            billingClient.launchBillingFlow(MainActivity.this, purchaseParams).getResponseCode();
                        }
                    }
                });


    }


    /**
     * 购买回调
     * @param billingResult
     * @param purchases
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //确认购买是成功的
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    /**
     * 这里处理返回
     * 1.服务器 购买信息，服务器确认
     * 2.消耗掉这个商品 purchase.getSku();
     * @param purchase
     */
    private void handlePurchase(Purchase purchase) {
        purchase.getOrderId();
        purchase.getSku();

        //消耗掉这个商品，防止下次购买不了
        consumePurchase(purchase.getPurchaseToken());
    }

    private void consumePurchase(String purchaseToken){
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();
    }

}