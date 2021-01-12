package com.bzm.googlenew.google.pay;

/**
 * Created by bzm0518
 * on 2021/1/12
 */
public class PayInfo {

    private String productId;

    private String orderId;

    private String money;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
