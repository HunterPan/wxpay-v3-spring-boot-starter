package com.jcidtech.pay.paypal.service;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.entity.Order;
import com.jcidtech.pay.common.error.ErrorCode;
import com.jcidtech.pay.paypal.base.PayPalClient;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springblade.core.tool.jackson.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PayPalService {
    private PayPalClient payPalClient;
    private ApplicationContext applicationContext;
    public PayPalService(PayPalClient payPalClient) {
        this.payPalClient = payPalClient;
        applicationContext = new ApplicationContext().brandName(payPalClient.properties().getBrandName()).landingPage("BILLING")
                .cancelUrl(payPalClient.properties().getCancelUrl()).returnUrl(payPalClient.properties().getReturnUrl()).userAction("CONTINUE")
                .shippingPreference("NO_SHIPPING");
    }

    /**
     * 创建订单
     * @param order
     * @param item
     * @return
     * @throws IOException
     */
    public com.paypal.orders.Order createOrder(Order order, ItemEntity item) throws IOException {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer","return=representation");
        request.requestBody(buildRequestBody(order,item));

        HttpResponse<com.paypal.orders.Order> response = payPalClient.client().execute(request);
        log.info("paypal create order result,status:{},order:{}", response.statusCode(), new JSONObject(new Json().serialize(response.result())).toString(4));
        if(response.statusCode() == 201){
            return response.result();
        }
        return null;
    }
    private OrderRequest buildRequestBody(Order order, ItemEntity item){
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<PurchaseUnitRequest>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest().referenceId(String.valueOf(order.getId()))
                .description("Virtual Goods").customId(String.valueOf(order.getId()))
                //AmountWithBreakdown = itemTotal + shipping+handling+taxTotal+shippingDiscount
                .amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(item.getPrice().toString())
                        .amountBreakdown(new AmountBreakdown()
                                .itemTotal(new Money().currencyCode("USD").value(item.getPrice().toString()))
                                .shipping(new Money().currencyCode("USD").value("0.00"))
                                .handling(new Money().currencyCode("USD").value("0.00"))
                                .taxTotal(new Money().currencyCode("USD").value("0.00"))
                                .shippingDiscount(new Money().currencyCode("USD").value("0.00"))))
                .items(new ArrayList<Item>() {
                    {
                        add(new Item().name(item.getTitle()).description(item.getRemark())
                                .unitAmount(new Money().currencyCode("USD").value(item.getPrice().toString()))
                                .quantity("1"));
                    }
                })
                .shippingDetail(new ShippingDetail().name(new Name().fullName("John Doe"))
                        .addressPortable(new AddressPortable().addressLine1("123 Townsend St").addressLine2("Floor 6")
                                .adminArea2("San Francisco").adminArea1("CA").postalCode("94107").countryCode("US")));;
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

    private OrderRequest buildRequestBody() {
        return new OrderRequest();
    }

    /**
     * 用户提交支付后后--扣款
     * @param orderId
     * @return
     * @throws IOException
     */
    public com.paypal.orders.Order captureOrder(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.requestBody(buildRequestBody());
        HttpResponse<com.paypal.orders.Order> response = payPalClient.client().execute(request);
        log.info("paypal capture order result,status:{},order:{}", JsonUtil.toJson(response),new JSONObject(new Json().serialize(response.result())).toString(4));
        return response.result();
    }

    /**
     * 获取订单详细
     * @param orderId
     * @throws IOException
     */
    public com.paypal.orders.Order getOrder(String orderId) throws IOException {
        OrdersGetRequest request = new OrdersGetRequest(orderId);
        HttpResponse<com.paypal.orders.Order> response = payPalClient.client().execute(request);
        log.info("paypal get order detail result,status:{},order:{}", JsonUtil.toJson(response),new JSONObject(new Json().serialize(response.result())).toString(4));
        return response.result();
    }
}
