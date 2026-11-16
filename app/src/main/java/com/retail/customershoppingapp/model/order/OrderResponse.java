package com.retail.customershoppingapp.model.order;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("orderGroupId")
    private String orderGroupId;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("sellerBusinessName")
    private String sellerBusinessName;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("customerMobile")
    private String customerMobile;

    @SerializedName("subtotalAmount")
    private Double subtotalAmount;

    @SerializedName("discountAmount")
    private Double discountAmount;

    @SerializedName("taxAmount")
    private Double taxAmount;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("returnedAmount")
    private Double returnedAmount;

    @SerializedName("paidAmount")
    private Double paidAmount;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("sellerRemarks")
    private String sellerRemarks;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("status")
    private String status;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("items")
    private List<OrderItemResponse> items;

    // Client-side list of individual seller sub-orders belonging to this Master Order
    private List<OrderResponse> subOrders;

    public Long getId() { return id; }
    public String getOrderGroupId() {
        return (orderGroupId != null && !orderGroupId.trim().isEmpty())
                ? orderGroupId.trim()
                : (id != null ? "FM-" + id : "FM-ORDER");
    }
    public Long getSellerId() { return sellerId; }
    public String getSellerBusinessName() { return sellerBusinessName; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public Double getSubtotalAmount() { return subtotalAmount != null ? subtotalAmount : getTotalAmount(); }
    public Double getDiscountAmount() { return discountAmount != null ? discountAmount : 0.0; }
    public Double getTaxAmount() { return taxAmount != null ? taxAmount : 0.0; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public Double getReturnedAmount() { return returnedAmount != null ? returnedAmount : 0.0; }
    public Double getPaidAmount() { return paidAmount != null ? paidAmount : 0.0; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public String getSellerRemarks() { return sellerRemarks; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status != null ? status : "PLACED"; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemResponse> getItems() { return items; }

    public List<OrderResponse> getSubOrders() {
        if (subOrders == null || subOrders.isEmpty()) {
            return Collections.singletonList(this);
        }
        return subOrders;
    }

    public int getSellerLotCount() {
        return getSubOrders().size();
    }

    public List<Long> getCancellableSubOrderIds() {
        List<Long> ids = new ArrayList<>();
        for (OrderResponse sub : getSubOrders()) {
            if (sub.getId() != null && "PLACED".equalsIgnoreCase(sub.getStatus())) {
                ids.add(sub.getId());
            }
        }
        return ids;
    }

    public double getNetPayableAmount() {
        return Math.max(0.0, getTotalAmount() - getReturnedAmount());
    }

    public double getBalanceDueAmount() {
        return Math.max(0.0, getNetPayableAmount() - getPaidAmount());
    }

    public double getRefundCreditAmount() {
        return Math.max(0.0, getPaidAmount() - getNetPayableAmount());
    }

    /**
     * Consolidates multiple Seller sub-orders that share the same orderGroupId
     * into a single Master OrderResponse card for the Buyer UI.
     */
    public static List<OrderResponse> groupByMasterOrder(List<OrderResponse> rawOrders) {
        if (rawOrders == null || rawOrders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<OrderResponse>> groupedMap = new LinkedHashMap<>();
        for (OrderResponse order : rawOrders) {
            if (order == null) continue;
            String key = order.getOrderGroupId();
            groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(order);
        }

        List<OrderResponse> consolidatedList = new ArrayList<>();
        for (Map.Entry<String, List<OrderResponse>> entry : groupedMap.entrySet()) {
            List<OrderResponse> group = entry.getValue();
            if (group.size() == 1) {
                OrderResponse single = group.get(0);
                single.subOrders = Collections.singletonList(single);
                consolidatedList.add(single);
            } else {
                consolidatedList.add(mergeSubOrders(entry.getKey(), group));
            }
        }
        return consolidatedList;
    }

    private static OrderResponse mergeSubOrders(String groupId, List<OrderResponse> group) {
        OrderResponse first = group.get(0);
        OrderResponse master = new OrderResponse();
        master.id = first.getId();
        master.orderGroupId = groupId;
        master.customerId = first.getCustomerId();
        master.customerName = first.getCustomerName();
        master.customerMobile = first.getCustomerMobile();
        master.orderDate = first.getOrderDate();
        master.deliveryAddress = first.getDeliveryAddress();
        master.subOrders = new ArrayList<>(group);

        double subtotal = 0.0;
        double discount = 0.0;
        double tax = 0.0;
        double total = 0.0;
        double returned = 0.0;
        double paid = 0.0;

        List<OrderItemResponse> mergedItems = new ArrayList<>();
        StringBuilder remarksBuilder = new StringBuilder();

        boolean allSameStatus = true;
        String firstStatus = first.getStatus().toUpperCase(Locale.ROOT);
        int highestActiveRank = -1;
        String highestActiveStatus = firstStatus;

        for (int idx = 0; idx < group.size(); idx++) {
            OrderResponse sub = group.get(idx);
            String subStatus = sub.getStatus().toUpperCase(Locale.ROOT);
            if (!subStatus.equals(firstStatus)) {
                allSameStatus = false;
            }
            int rank = statusRank(subStatus);
            if (rank > highestActiveRank) {
                highestActiveRank = rank;
                highestActiveStatus = subStatus;
            }

            subtotal += sub.getSubtotalAmount();
            discount += sub.getDiscountAmount();
            tax += sub.getTaxAmount();
            total += sub.getTotalAmount();
            returned += sub.getReturnedAmount();
            paid += sub.getPaidAmount();

            if (sub.getSellerRemarks() != null && !sub.getSellerRemarks().trim().isEmpty()) {
                if (remarksBuilder.length() > 0) remarksBuilder.append(" | ");
                remarksBuilder.append("Lot #").append(sub.getId()).append(": ").append(sub.getSellerRemarks().trim());
            }

            if (sub.getItems() != null) {
                for (OrderItemResponse item : sub.getItems()) {
                    OrderItemResponse copy = new OrderItemResponse();
                    copy.setId(item.getId());
                    copy.setProductId(item.getProductId());
                    copy.setProductName(item.getProductName());
                    copy.setVariantId(item.getVariantId());
                    copy.setSku(item.getSku());
                    copy.setSize(item.getSize());
                    copy.setColor(item.getColor());
                    copy.setQuantity(item.getQuantity());
                    copy.setPrice(item.getPrice());

                    String lotTag = "Lot #" + sub.getId() + " (" + subStatus + ")";
                    if (item.getItemNote() != null && !item.getItemNote().trim().isEmpty()) {
                        copy.setItemNote(lotTag + " • " + item.getItemNote().trim());
                    } else {
                        copy.setItemNote(lotTag);
                    }
                    mergedItems.add(copy);
                }
            }
        }

        master.subtotalAmount = subtotal;
        master.discountAmount = discount;
        master.taxAmount = tax;
        master.totalAmount = total;
        master.returnedAmount = returned;
        master.paidAmount = paid;
        master.items = mergedItems;
        master.sellerRemarks = remarksBuilder.length() > 0 ? remarksBuilder.toString() : null;
        master.status = allSameStatus ? firstStatus : highestActiveStatus;

        double netPayable = Math.max(0.0, total - returned);
        if (paid <= 0.009) {
            master.paymentStatus = "UNPAID";
        } else if (paid >= netPayable - 0.009) {
            master.paymentStatus = "PAID";
        } else {
            master.paymentStatus = "PARTIALLY_PAID";
        }

        return master;
    }

    private static int statusRank(String status) {
        if (status == null) return 0;
        switch (status.toUpperCase(Locale.ROOT)) {
            case "DELIVERED": return 5;
            case "SHIPPED": return 4;
            case "PROCESSING": return 3;
            case "CONFIRMED": return 2;
            case "PLACED": return 1;
            default: return 0; // CANCELLED or REJECTED
        }
    }
}