package com.bzm.googlenew.google.pay;

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
 * 官方文档:https://developer.android.google.cn/google/play/billing/integrate#ooap
 * 从老版本到新版本(aidl版本到最新版本改变):https://developer.android.google.cn/google/play/billing/migrate
 * aidl版本2020年年末就取消使用了
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

    private GooglePayHelper(){
    }

    private static GooglePayHelper mInstance;

    public static GooglePayHelper getInstance(){
        if (mInstance == null){
            synchronized (GooglePayHelper.class){
                if (mInstance == null){
                    mInstance = new GooglePayHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     * @param activity
     */
    public void initialize(Activity activity){
        this.mCurrentActivity = activity;
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
            //没有链接到Google的服务，做重连
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
            //没有链接到Google的服务，做重连
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

    /**
     * 查询所有未消耗的商品
     */
    private void queryPurchases(){
        if (!billingClient.isReady()){
            //没有链接到Google的服务，做重连
            return;
        }
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (result == null){
            return;
        }else {
            if (result.getPurchasesList() != null){
                //走消耗流程
                for (Purchase purchase : result.getPurchasesList()) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                        //消耗
                        handlePurchase(purchase);
                    }
                }
            }else {
                //无可消耗商品
            }
        }
    }



}
