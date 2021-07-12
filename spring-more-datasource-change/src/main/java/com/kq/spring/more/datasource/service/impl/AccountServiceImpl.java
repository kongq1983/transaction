package com.kq.spring.more.datasource.service.impl;

import com.kq.spring.more.datasource.config.DataSourceKey;
import com.kq.spring.more.datasource.config.DynamicDataSourceContextHolder;
import com.kq.spring.more.datasource.entity.Account;
import com.kq.spring.more.datasource.mapper.AccountMapper;
import com.kq.spring.more.datasource.service.AccountService;

import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Fox
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    
    private static final String ERROR_USER_ID = "1002";
    
    @Autowired
    private AccountMapper accountMapper;
    
    /**
     * 扣减用户金额
     * @param userId
     * @param money
     */
    @Transactional//(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void debit(String userId, int money){
        log.info("=============用户账户扣款=================");
        //切换数据源
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.ACCOUNT);
        log.info("当前 XID: {}", RootContext.getXID());
    
        //检查用户余额
        checkBalance(userId, money);
        
        log.info("开始扣减用户 {} 余额", userId);
        Integer record = accountMapper.reduceBalance(userId,money);
        
//        if (ERROR_USER_ID.equals(userId)) {
//            // 模拟异常
//            throw new RuntimeException("account branch exception");
//        }
        log.info("扣减用户 {} 余额结果:{}", userId, record > 0 ? "操作成功" : "扣减余额失败");
    }
    
    private void checkBalance(String userId, int money){
        log.info("检查用户 {} 余额", userId);
        Account account = accountMapper.selectByUserId(userId);
        if(account == null){
            throw new RuntimeException("账户不存在");
        }
        if (account.getMoney() < money) {
            log.warn("用户 {} 余额不足，当前余额:{}", userId, account.getMoney());
            throw new RuntimeException("余额不足");
        }
        
    }
}
