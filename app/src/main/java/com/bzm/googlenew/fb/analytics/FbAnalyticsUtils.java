package com.bzm.googlenew.fb.analytics;

import android.content.Context;
import android.os.Bundle;

import com.bzm.googlenew.fb.login.FbSignInHelper;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

/**
 * Created by bzm0518
 * on 2020/8/7
 * github地址:https://github.com/facebook/facebook-android-sdk
 */
public class FbAnalyticsUtils {
    private AppEventsLogger sAppEventsLogger;

    private static FbAnalyticsUtils mInstance = new FbAnalyticsUtils();

    private FbAnalyticsUtils(){
    }

    public static FbAnalyticsUtils getInstance(){
        return mInstance;
    }

    public void init(Context context){
        sAppEventsLogger = AppEventsLogger.newLogger(context);
    }

    /**
     * 报送充值事件给Facebook
     *
     * @param orderAmount 订单金额 币种是美金
     */
    public void fbLogPurchaseEvent(String orderAmount) {
        try {
            double revenue = Double.parseDouble(orderAmount);
            sAppEventsLogger.logPurchase(BigDecimal.valueOf(revenue), Currency.getInstance("USD"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * fb 注册报送
     */
    public void fbRegister(){
        Bundle params = new Bundle();
        //如果fb没有注册事件的话，启动自定义事件 logEvent
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "facebook_login");
        sAppEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }


    /**
     * FB自定义事件
     * @param event
     * @param values
     */
    public void fbCustomEvent(String event, Map<String ,String > values){
        //获取参数Key
        Set<String> keySet = values.keySet();
        Bundle params = new Bundle();
        //遍历key 添加到FB的参数
        for (String key: keySet) {
            params.putString(key, values.get(key));
        }
        sAppEventsLogger.logEvent(event, params);
    }


}
