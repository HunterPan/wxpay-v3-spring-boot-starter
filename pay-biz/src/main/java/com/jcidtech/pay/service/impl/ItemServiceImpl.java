package com.jcidtech.pay.service.impl;

import com.jcidech.mp.base.BaseServiceImpl;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.enums.ItemStatus;
import com.jcidtech.pay.mapper.ItemEntityMapper;
import com.jcidtech.pay.service.IItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ItemServiceImpl extends BaseServiceImpl<ItemEntityMapper, ItemEntity> implements IItemService {
    @Override
    public boolean saveVisual(ItemEntity item) {
       this.save(item);
       return true;
    }

    @Override
    public boolean putOn(Long id) {
        ItemEntity item  = new ItemEntity();
        item.setId(id);
        item.setStatus(ItemStatus.ON.getValue());
        this.updateById(item);
        return true;
    }

    @Override
    public boolean putOff(Long id) {
        ItemEntity item  = new ItemEntity();
        item.setId(id);
        item.setStatus(ItemStatus.OFF.getValue());
        this.updateById(item);
        return true;
    }
}
