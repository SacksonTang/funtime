package com.rzyou.funtime.service;

import java.util.Map;

public interface PayService {

    Map<String, String> unifiedOrder(String ip, String imei, String orderId);


}
