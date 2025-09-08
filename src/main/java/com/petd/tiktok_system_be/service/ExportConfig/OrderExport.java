package com.petd.tiktok_system_be.service.ExportConfig;

import com.petd.tiktok_system_be.entity.Address;
import com.petd.tiktok_system_be.entity.Design;
import com.petd.tiktok_system_be.entity.Order;
import com.petd.tiktok_system_be.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderExport {
    private String acc;
    private String createdTime;
    private String firstName;
    private String lastName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private String sellerOrderId;
    private String productId;
    private String front;
    private String back;
    private String left;
    private String right;
    private Integer quantity;
    private String size;
    private String color;
    private String tracking;
    private String prepaidLabel;
    private String productName;
    private String linkSp;
    private String linkMain;
    private String orderId;
    private String orderFree;
    private String orderAmount;
    private String productColor;
    private String key;





    public OrderExport(Order  order, Integer quantity, OrderItem item, Design design){

        List<Address> addresses = order.getRecipientAddress().getDistrictInfo();

        String country = addresses.stream()
                .filter(a -> "L0".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        String state = addresses.stream()
                .filter(a -> "L1".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        String city = addresses.stream()
                .filter(a -> "L3".equals(a.getAddress_level()))
                .map(Address::getAddress_name)
                .findFirst().orElse(null);

        String[] parts = item.getSkuName().split(",");

        this.acc = order.getShop().getUserShopName();
        this.createdTime =  convertToVietnamTime(order.getCreateTime());
        this.firstName = order.getRecipientAddress().getFirstName();
        this.lastName = order.getRecipientAddress().getLastName();
        this.address1 = order.getRecipientAddress().getAddressLine1();
        this.address2 = order.getRecipientAddress().getAddressLine2();
        this.city = city;
        this.state = StateMapper.getAbbreviation(state);
        this.country = country;
        this.zip = order.getRecipientAddress().getPostalCode();
        this.phone = order.getRecipientAddress().getPhoneNumber();
        this.sellerOrderId = order.getId();
        this.quantity = quantity;

        this.size = parts[1];
        this.color = parts[0];

        this.productId = "Classic T-Shirt";
        this.tracking = order.getTrackingNumber();
        this.prepaidLabel = order.getLabel();
        this.productName = item.getProductName();
        this.linkSp = "https://shop.tiktok.com/view/product/" + item.getProductId();
        this.linkMain = item.getSkuImage();
        this.orderId = "";
        this.orderFree = "";
        this.orderAmount = order.getPaymentAmount().toString();
        this.key = item.getSkuId() + "-" + order.getId();
        if(design != null){
            this.front = design.getFrontSide();
            this.back = design.getBackSide();
            this.left = design.getLeftSide();
            this.right = design.getRightSide();
        }
    }

    public String convertToVietnamTime(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime vnTime = instant.atZone(vietnamZone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        return vnTime.format(formatter);
    }

}
