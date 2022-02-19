package com.jcidtech.pay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcidech.mp.support.Condition;
import com.jcidech.mp.support.Query;
import com.jcidtech.pay.common.entity.ItemEntity;
import com.jcidtech.pay.common.enums.ItemStatus;
import com.jcidtech.pay.common.enums.PayChannel;
import com.jcidtech.pay.common.vo.ItemForUserVO;
import com.jcidtech.pay.common.vo.ItemVO;
import com.jcidtech.pay.service.IItemService;
import com.jcidtech.pay.utils.QRCodeUtil;
import com.jcidtech.pay.webconf.SysAdmin;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("item")
@Slf4j
public class ItemController {
    @Resource
    private IItemService itemService;
    @SysAdmin
    @GetMapping("/list-for-admin")
    public R<IPage<ItemVO>> list(ItemEntity item, Query query) {
        IPage<ItemEntity> list = itemService.page(Condition.getPage(query),Condition.getQueryWrapper(item));
        IPage<ItemVO> itemVOIPage = new Page<>();
        BeanUtils.copyProperties(list,itemVOIPage);
        List<ItemVO> itemVOList = Collections.emptyList();
        if(CollectionUtil.isNotEmpty(list.getRecords())){
            itemVOList = list.getRecords().stream().map(itemEntity->{
                ItemVO itemVO = BeanUtil.copy(itemEntity,ItemVO.class);
                ItemStatus itemStatus = ItemStatus.getByStatus(itemEntity.getStatus());
                if(Objects.nonNull(itemStatus)){
                    itemVO.setStatusName(itemStatus.getRemark());
                }else {
                    itemVO.setStatusName(ItemStatus.UNKNOWN.getRemark());
                }
                return itemVO;
            }).collect(Collectors.toList());
        }
        itemVOIPage.setRecords(itemVOList);
        return R.data(itemVOIPage);
    }
    @GetMapping("/list-for-user")
    public R<IPage<ItemForUserVO>> listForUser(ItemEntity item, Query query) {
        item.setStatus(ItemStatus.ON.getValue());
        IPage<ItemEntity> list = itemService.page(Condition.getPage(query),Condition.getQueryWrapper(item));
        IPage<ItemForUserVO> itemVOIPage = new Page<>();
        BeanUtils.copyProperties(list,itemVOIPage);
        List<ItemForUserVO> itemVOList = Collections.emptyList();
        if(CollectionUtil.isNotEmpty(list.getRecords())){
            itemVOList = list.getRecords().stream().map(itemEntity->{
                ItemForUserVO itemVO = BeanUtil.copy(itemEntity,ItemForUserVO.class);
                return itemVO;
            }).collect(Collectors.toList());
        }
        itemVOIPage.setRecords(itemVOList);
        return R.data(itemVOIPage);
    }
    /*@GetMapping("/qr")
    public R<String> qr(@RequestParam Long id,@RequestParam String payChannel) {
        ItemEntity item = itemService.getById(id);
        return R.data(QRCodeUtil.generateQRCode(String.format(host,item.getId(), PayChannel.WX.getChannel()),300,300));
    }*/
    @GetMapping("/detail")
    public R<ItemEntity> detail(@RequestParam Long id) {
        ItemEntity item = itemService.getById(id);
        return R.data(item);
    }
    @SysAdmin
    @PostMapping("/add")
    public R<Long> add(@RequestBody ItemEntity item) {
        boolean result = itemService.save(item);
        return R.data(result?item.getId():0l);
    }
    @SysAdmin
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody ItemEntity item) {
        boolean result = itemService.updateById(item);
        return R.data(result);
    }
    @SysAdmin
    @PostMapping("/put-on")
    public R<Boolean> putOn(@RequestParam Long id) {
        boolean result = itemService.putOn(id);
        return R.data(result);
    }
    @SysAdmin
    @PostMapping("/put-off")
    public R<Boolean> putOff(@RequestParam Long id) {
        boolean result = itemService.putOff(id);
        return R.data(result);
    }
    @SysAdmin
    @PostMapping("/remove")
    public R<Boolean> remove(@RequestParam Long id) {
        boolean result = itemService.deleteLogic(Arrays.asList(id));
        return R.data(result);
    }
}
