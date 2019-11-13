package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;

public interface AccountService {
    void recharge(FuntimeUserAccountRechargeRecord record);

    void paySuccess(Long orderId);

    PageInfo<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId);
}
