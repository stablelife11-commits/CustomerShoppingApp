package com.retail.customershoppingapp.model.cart;

import com.google.gson.annotations.SerializedName;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartResponseDto {

    @SerializedName("cartId")
    private Long cartId;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("totalDistinctItems")
    private Integer totalDistinctItems;

    @SerializedName("totalQuantity")
    private Integer totalQuantity;

    @SerializedName("estimatedTotalAmount")
    private BigDecimal estimatedTotalAmount;

    @SerializedName("items")
    private List<ServerCartItemDto> items;

    public List<CartItem> toLocalCartItems() {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<CartItem> result = new ArrayList<>();
        for (ServerCartItemDto dto : items) {
            VariantResponse variant = new VariantResponse();
            variant.setId(dto.variantId);
            variant.setSku(dto.sku);
            variant.setSize(dto.size);
            variant.setColor(dto.color);
            variant.setHsnCode(dto.hsnCode);
            variant.setGstPercentage(dto.gstPercentage);
            variant.setSellingPrice(dto.unitPrice);
            variant.setStock(dto.availableStock != null ? dto.availableStock : 0);
            variant.setMinimumOrderQuantity(dto.minimumOrderQuantity != null ? dto.minimumOrderQuantity : 1);
            variant.setPackSize(dto.packSize != null ? dto.packSize : 1);

            ProductResponse product = new ProductResponse();
            product.setId(dto.productId);
            product.setProductCode(dto.productCode);
            product.setName(dto.productName);
            product.setBrand(dto.brand);
            product.setCategory(dto.category);
            product.setSellerId(dto.sellerId);
            product.setActive(dto.productActive != null ? dto.productActive : true);
            product.setVariants(Collections.singletonList(variant));

            if (dto.primaryImageUrl != null && !dto.primaryImageUrl.isEmpty()) {
                product.setImageUrls(Collections.singletonList(dto.primaryImageUrl));
            }

            int qty = dto.quantity != null ? dto.quantity : 1;
            result.add(new CartItem(product, variant, qty, dto.itemNote));
        }
        return result;
    }

    public static class ServerCartItemDto {
        @SerializedName("id")
        public Long id;

        @SerializedName("productId")
        public Long productId;

        @SerializedName("productCode")
        public String productCode;

        @SerializedName("productName")
        public String productName;

        @SerializedName("brand")
        public String brand;

        @SerializedName("category")
        public String category;

        @SerializedName("sellerId")
        public Long sellerId;

        @SerializedName("primaryImageUrl")
        public String primaryImageUrl;

        @SerializedName("variantId")
        public Long variantId;

        @SerializedName("sku")
        public String sku;

        @SerializedName("size")
        public String size;

        @SerializedName("color")
        public String color;

        @SerializedName("hsnCode")
        public String hsnCode;

        @SerializedName("gstPercentage")
        public BigDecimal gstPercentage;

        @SerializedName("unitPrice")
        public BigDecimal unitPrice;

        @SerializedName("quantity")
        public Integer quantity;

        @SerializedName("availableStock")
        public Integer availableStock;

        @SerializedName("minimumOrderQuantity")
        public Integer minimumOrderQuantity;

        @SerializedName("packSize")
        public Integer packSize;

        @SerializedName("productActive")
        public Boolean productActive;

        @SerializedName("itemNote")
        public String itemNote;
    }
}