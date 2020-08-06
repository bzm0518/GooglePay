package com.bzm.googlenew;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bzm0518
 * on 2020/8/6
 */
public class GooglePayHelper implements PurchasesUpdatedListener {

    /**
     * 参考文档:
     *  1.https://www.jianshu.com/p/0375402f7a2c
     *  2.https://blog.csdn.net/u013334392/article/details/102584801
     *  3.https://www.jianshu.com/p/1a58d918ae74
     */

    private BillingClient billingClient;

    private Activity mCurrentActivity;

    public GooglePayHelper(Activity activity){
        this.mCurrentActivity = activity;
    }



    public void initialize(){
        //初始化BillingClient
        billingClient = BillingClient.newBuilder(mCurrentActivity)
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
    public void pay(final String sku) throws JSONException {
        if (!billingClient.isReady()){
            //没有链接到Google的服务
            return;
        }
        //sku 唯一商品 ID（也称为 SKU）。必须以小写字母或数字开头，并且只能由小写字母 (a-z)、数字 (0-9)、下划线 (_) 和句点 (.) 组成
        //可包名_金额 这种格式 com.bzm.google_0.99
        List<String> skuList = new ArrayList<>();
        skuList.add(sku);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && skuDetailsList != null){
                            // SkuDetails object obtained above.
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.equals(sku)){

                                    BillingFlowParams purchaseParams =
                                            BillingFlowParams.newBuilder()
                                                    .setSkuDetails(skuDetails)
                                                    .build();

                                    billingClient.launchBillingFlow(mCurrentActivity, purchaseParams).getResponseCode();

                                }
                            }


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
     * 2.消耗掉这个商品 purchase.getPurchaseToken();
     * @param purchase
     */
    private void handlePurchase(Purchase purchase) {
        purchase.getOrderId();
        purchase.getSku();

        //消耗掉这个商品，防止下次购买不了
        consumePurchase(purchase.getPurchaseToken());
    }

    /**
     * 消耗商品 （如果不消耗掉的话，支付成功状态三天后会自动退款）
     * @param purchaseToken
     */
    public void consumePurchase(String purchaseToken){
        if (!billingClient.isReady()){
            //没有链接到Google的服务
            return;
        }
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                //消耗回调
            }
        });
    }






}
