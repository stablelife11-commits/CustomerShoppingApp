package com.retail.customershoppingapp.model.product;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class VariantResponse implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("sku")
    private String sku;

    @SerializedName("size")
    private String size;

    @SerializedName("color")
    private String color;

    @SerializedName("sellingPrice")
    private BigDecimal sellingPrice;

    @SerializedName("purchasePrice")
    private BigDecimal purchasePrice;

    @SerializedName("stock")
    private Integer stock;

    @SerializedName("minimumOrderQuantity")
    private Integer minimumOrderQuantity;

    @SerializedName("packSize")
    private Integer packSize;

    @SerializedName("hsnCode")
    private String hsnCode;

    @SerializedName("gstPercentage")
    private BigDecimal gstPercentage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getStock() {
        return stock != null ? stock : 0;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public int getMinimumOrderQuantity() {
        return minimumOrderQuantity != null && minimumOrderQuantity > 0 ? minimumOrderQuantity : 1;
    }

    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public int getPackSize() {
        return packSize != null && packSize > 0 ? packSize : 1;
    }

    public void setPackSize(Integer packSize) {
        this.packSize = packSize;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public BigDecimal getGstPercentage() {
        return gstPercentage != null ? gstPercentage : BigDecimal.ZERO;
    }

    public void setGstPercentage(BigDecimal gstPercentage) {
        this.gstPercentage = gstPercentage;
    }
}