package com.bzm.googlenew.google.pay;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AccountIdentifiers;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bzm0518
 * on 2020/11/14
 */
public class GooglePayHelper implements PurchasesUpdatedListener,SkuDetailsResponseListener {
    /**
     * 参考文档:
     *  1.https://www.jianshu.com/p/0375402f7a2c
     *  2.https://blog.csdn.net/u013334392/article/details/102584801
     *  3.https://www.jianshu.com/p/1a58d918ae74
     */

    private BillingClient billingClient;

    private WeakReference<Activity> mCurrentActivity;

    //预先保存好所有的订单信息
    private HashMap<String,SkuDetails> mProducts = new HashMap<>();
    //是否是调起支付的
    private boolean mIsPay;

    //google 商品
    private String mSku;
    //支付信息
    private PayInfo mPayInfo;

    private IPayCallback<Purchase> mCallback;

    private GooglePayHelper(){
    }

    private static volatile GooglePayHelper mInstance;

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

    private void initialize(){
        if (mCurrentActivity == null)return;
        this.initialize(mCurrentActivity.get(),null);
    }

    /**
     * 初始化
     * @param activity
     */
    public void initialize(Activity activity, final List<String> skuList){
        if (billingClient != null && billingClient.isReady()){
            //如果已经连接了，不需要再次连接
            return;
        }
        this.mCurrentActivity = new WeakReference<>(activity);
        //初始化BillingClient
        billingClient = BillingClient.newBuilder(mCurrentActivity.get())
                //监听支付
                .setListener(this)
                // 为了支持待处理的购买交易，请在初始化您的应用期间调用 enablePendingPurchases()
                .enablePendingPurchases()
                .build();
        //建立与Google Play的连接
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // 已连接到google，可以支付或查询
                    //如果skuList不为null 全部sku查出来保存在内存中
                    if (!skuList.isEmpty()){
                        querySku(skuList);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }


    /**
     * 外放google pay接口
     * @param activity
     */
    public void startGooglePay(Activity activity, PayInfo info,IPayCallback<Purchase> callback){
        mCallback = callback;
        this.mSku = info.getProductId();
        this.mPayInfo = info;
        mCurrentActivity = new WeakReference<>(activity);
        if (billingClient == null || !billingClient.isReady()){
            Log.e("GooglePayHelper", "startGooglePay: 连接失败"  );
            //没有连接成功，需要重新连接
            mIsPay = true;
            List<String> list = new ArrayList<>();
            list.add(mSku);
            initialize(activity,list);
            return;
        }

        checkPay(mSku);
    }

    /**
     * 检查 sku并调起支付
     * @param sku
     */
    private void checkPay(final String sku){
        Log.e("====", "pay: " + sku );

        SkuDetails skuDetails;
        //如果内存里面没有值 或者 内存里面没有这个商品 先查询再购买
        if (mProducts.isEmpty() || !mProducts.containsKey(sku)){
            mIsPay = true;
            //执行查询订单,再调起支付
            List<String> skuList = new ArrayList<>();
            skuList.add(sku);
            querySku(skuList);
            return;
        }

        skuDetails = mProducts.get(sku);
        googlePay(skuDetails);
    }

    /**
     * 查询商品详情
     * @param skuList
     */
    private void querySku(List<String> skuList){
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(), this);
    }

    /**
     * 执行google支付
     * @param skuDetails
     */
    private void googlePay(SkuDetails skuDetails){
        //调起支付再把mIsPay设置为原始值
        mIsPay = false;
        BillingFlowParams purchaseParams =
                BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .setObfuscatedAccountId(mPayInfo.getMoney())//透传字段 可在purchase.getOriginalJson()中获取到 obfuscatedAccountId
                        .setObfuscatedProfileId(mPayInfo.getOrderId())//透传字段可在purchase.getOriginalJson()中获取到 obfuscatedProfileId
                        .build();

        int code = billingClient.launchBillingFlow(mCurrentActivity.get(), purchaseParams).getResponseCode();
    }

    @Override
    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
            for (SkuDetails skuDetails : list) {
                if (mIsPay){
                    //因为表里面没有，所以先查询再支付
                    googlePay(skuDetails);
                    //保存
                    mProducts.put(skuDetails.getSku(),skuDetails);
                }
                //保存订单到内存中
                mProducts.put(skuDetails.getSku(),skuDetails);
            }
        }else {
            //如果是支付状态，查询根据商品id 查找不到 返回支付失败
            if (mIsPay && mCallback != null){
                //回调失败
                mCallback.fail(billingResult.getDebugMessage());
            }
        }
    }

    /**
     * 支付回调
     * @param billingResult
     * @param purchases
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //确认购买是成功的
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            if(mCallback == null )return;
            //支付取消
            mCallback.fail("支付取消");
        } else {
            if(mCallback == null )return;
            // Handle any other error codes.
            //其他支付失败问题
            mCallback.fail("pay code:" + billingResult.getResponseCode() + ",msg:" + billingResult.getDebugMessage());
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
        if(mCallback == null )return;
        //回调成功
        mCallback.success(purchase);
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
    public void queryAllNotConsumedPurchases(IPayCallback<Purchase> callBack){
        if (billingClient == null || !billingClient.isReady()){
            Log.e("GooglePayHelper", "queryAllNotConsumedPurchases: 连接失败");
            //没有连接成功，需要重新连接
            initialize();
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
                        //已购买
                        //回调成功
                        callBack.success(purchase);
                    }else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
                        callBack.fail(result.getBillingResult().getDebugMessage());
                    }
                }
            }else {
                //无可消耗商品
                callBack.fail("无可消耗商品");
            }
        }
    }

    /**
     * 退出时关闭连接
     */
    public void unBindService(){
        //退出时解绑
        if (billingClient != null){
            billingClient.endConnection();
            billingClient = null;
        }

    }


}
