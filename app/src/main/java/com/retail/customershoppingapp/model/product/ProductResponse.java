package com.retail.customershoppingapp.model.product;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ProductResponse implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("productCode")
    private String productCode;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("brand")
    private String brand;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("variants")
    private List<VariantResponse> variants;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<VariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantResponse> variants) {
        this.variants = variants;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Helper method to get minimum selling price from variants
    public BigDecimal getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal min = null;
        for (VariantResponse v : variants) {
            if (v.getSellingPrice() != null) {
                if (min == null || v.getSellingPrice().compareTo(min) < 0) {
                    min = v.getSellingPrice();
                }
            }
        }
        return min != null ? min : BigDecimal.ZERO;
    }

    // Helper method to get primary image URL
    public String getPrimaryImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }
}
