package com.jcidtech.pay.service;

import com.jcidech.mp.base.BaseService;
import com.jcidtech.pay.common.entity.ItemEntity;
public interface IItemService extends BaseService<ItemEntity> {
    boolean saveVisual(ItemEntity item);
    boolean putOn(Long id);
    boolean putOff(Long id);
}
