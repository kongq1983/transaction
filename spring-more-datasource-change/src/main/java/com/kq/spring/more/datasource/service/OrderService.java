package com.kq.spring.more.datasource.service;


import com.kq.spring.more.datasource.entity.Order;
import com.kq.spring.more.datasource.vo.OrderVo;

public interface OrderService {

    /**
     * 保存订单
     */
    Order saveOrder(OrderVo orderVo);
}