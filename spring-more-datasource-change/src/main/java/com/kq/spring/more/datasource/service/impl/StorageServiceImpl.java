package com.kq.spring.more.datasource.service.impl;


import com.kq.spring.more.datasource.config.DataSourceKey;
import com.kq.spring.more.datasource.config.DynamicDataSourceContextHolder;
import com.kq.spring.more.datasource.entity.Storage;
import com.kq.spring.more.datasource.mapper.StorageMapper;
import com.kq.spring.more.datasource.service.StorageService;

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
public class StorageServiceImpl implements StorageService {
    
    @Autowired
    private StorageMapper storageMapper;
    
    @Transactional//(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deduct(String commodityCode, int count){
        log.info("=============扣减库存=================");
        //切换数据源
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.STORAGE);
        log.info("当前 XID: {}", RootContext.getXID());
        // 检查库存
        checkStock(commodityCode,count);
        
        log.info("开始扣减 {} 库存", commodityCode);
        Integer record = storageMapper.reduceStorage(commodityCode,count);
        log.info("扣减 {} 库存结果:{}", commodityCode, record > 0 ? "操作成功" : "扣减库存失败");
    }
    
    private void checkStock(String commodityCode, int count){
        
        log.info("检查 {} 库存", commodityCode);
        Storage storage = storageMapper.findByCommodityCode(commodityCode);
        if(storage == null){
            throw new RuntimeException("库存不存在");
        }
        if (storage.getCount() < count) {
            log.warn("{} 库存不足，当前库存:{}", commodityCode, count);
            throw new RuntimeException("库存不足");
        }
        
    }
    
}
