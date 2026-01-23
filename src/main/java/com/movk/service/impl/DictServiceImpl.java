/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.config.CacheConfig;
import com.movk.common.enums.EnableStatus;
import com.movk.dto.dict.*;
import com.movk.entity.DictData;
import com.movk.entity.DictType;
import com.movk.repository.DictDataRepository;
import com.movk.repository.DictTypeRepository;
import com.movk.service.DictService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 字典服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeRepository dictTypeRepository;
    private final DictDataRepository dictDataRepository;

    // ========== 字典类型操作 ==========

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DICT_TYPE, allEntries = true)
    public UUID createDictType(DictTypeCreateReq req) {
        // 检查字典类型是否已存在
        if (existsByDictType(req.dictType())) {
            throw new IllegalArgumentException("字典类型已存在: " + req.dictType());
        }

        DictType dictType = DictType.builder()
            .dictName(req.dictName())
            .dictType(req.dictType())
            .status(req.status() != null ? req.status() : EnableStatus.ENABLED)
            .remark(req.remark())
            .build();

        dictTypeRepository.save(dictType);
        log.info("创建字典类型成功: {}", dictType.getDictType());
        return dictType.getId();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.DICT_TYPE, allEntries = true),
        @CacheEvict(value = CacheConfig.DICT_DATA, allEntries = true)
    })
    public void updateDictType(UUID id, DictTypeUpdateReq req) {
        DictType dictType = dictTypeRepository.findById(id)
            .filter(d -> !d.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("字典类型不存在"));

        // 如果修改了字典类型编码，需要同步更新字典数据
        String oldDictType = dictType.getDictType();
        if (!oldDictType.equals(req.dictType())) {
            // 检查新的字典类型是否已存在
            if (existsByDictType(req.dictType())) {
                throw new IllegalArgumentException("字典类型已存在: " + req.dictType());
            }
            // 更新关联的字典数据
            List<DictData> dataList = dictDataRepository.findByDictTypeAndDeletedFalseOrderByDictSortAsc(oldDictType);
            dataList.forEach(data -> data.setDictType(req.dictType()));
            dictDataRepository.saveAll(dataList);
        }

        dictType.setDictName(req.dictName());
        dictType.setDictType(req.dictType());
        dictType.setStatus(req.status());
        dictType.setRemark(req.remark());

        dictTypeRepository.save(dictType);
        log.info("更新字典类型成功: {}", dictType.getDictType());
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.DICT_TYPE, allEntries = true),
        @CacheEvict(value = CacheConfig.DICT_DATA, allEntries = true)
    })
    public void deleteDictType(UUID dictTypeId) {
        DictType dictType = dictTypeRepository.findById(dictTypeId)
            .filter(d -> !d.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("字典类型不存在"));

        // 逻辑删除字典类型
        dictType.setDeleted(true);
        dictType.setDeletedAt(OffsetDateTime.now());
        dictTypeRepository.save(dictType);

        // 逻辑删除关联的字典数据
        List<DictData> dataList = dictDataRepository.findByDictTypeAndDeletedFalseOrderByDictSortAsc(dictType.getDictType());
        dataList.forEach(data -> {
            data.setDeleted(true);
            data.setDeletedAt(OffsetDateTime.now());
        });
        dictDataRepository.saveAll(dataList);

        log.info("删除字典类型成功: {}", dictType.getDictType());
    }

    @Override
    public DictTypeResp getDictTypeById(UUID dictTypeId) {
        return dictTypeRepository.findById(dictTypeId)
            .filter(d -> !d.getDeleted())
            .map(this::toTypeResp)
            .orElseThrow(() -> new EntityNotFoundException("字典类型不存在"));
    }

    @Override
    public List<DictTypeResp> getAllDictTypes() {
        return dictTypeRepository.findByDeletedFalseOrderByCreatedAtDesc()
            .stream()
            .map(this::toTypeResp)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDictType(String dictType) {
        return dictTypeRepository.findByDictTypeAndDeletedFalse(dictType).isPresent();
    }

    // ========== 字典数据操作 ==========

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DICT_DATA, key = "#req.dictType()")
    public UUID createDictData(DictDataCreateReq req) {
        DictData dictData = DictData.builder()
            .dictType(req.dictType())
            .dictLabel(req.dictLabel())
            .dictValue(req.dictValue())
            .dictSort(req.dictSort() != null ? req.dictSort() : 0)
            .cssClass(req.cssClass())
            .listClass(req.listClass())
            .isDefault(req.isDefault() != null ? req.isDefault() : false)
            .status(req.status() != null ? req.status() : EnableStatus.ENABLED)
            .remark(req.remark())
            .build();

        dictDataRepository.save(dictData);
        log.info("创建字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
        return dictData.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DICT_DATA, allEntries = true)
    public void updateDictData(UUID id, DictDataUpdateReq req) {
        DictData dictData = dictDataRepository.findById(id)
            .filter(d -> !d.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("字典数据不存在"));

        dictData.setDictType(req.dictType());
        dictData.setDictLabel(req.dictLabel());
        dictData.setDictValue(req.dictValue());
        dictData.setDictSort(req.dictSort());
        dictData.setCssClass(req.cssClass());
        dictData.setListClass(req.listClass());
        dictData.setIsDefault(req.isDefault());
        dictData.setStatus(req.status());
        dictData.setRemark(req.remark());

        dictDataRepository.save(dictData);
        log.info("更新字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DICT_DATA, allEntries = true)
    public void deleteDictData(UUID dictDataId) {
        DictData dictData = dictDataRepository.findById(dictDataId)
            .filter(d -> !d.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("字典数据不存在"));

        dictData.setDeleted(true);
        dictData.setDeletedAt(OffsetDateTime.now());
        dictDataRepository.save(dictData);

        log.info("删除字典数据成功: {} - {}", dictData.getDictType(), dictData.getDictLabel());
    }

    @Override
    public DictDataResp getDictDataById(UUID dictDataId) {
        return dictDataRepository.findById(dictDataId)
            .filter(d -> !d.getDeleted())
            .map(this::toDataResp)
            .orElseThrow(() -> new EntityNotFoundException("字典数据不存在"));
    }

    @Override
    @Cacheable(value = CacheConfig.DICT_DATA, key = "#dictType")
    public List<DictDataResp> getDictDataByType(String dictType) {
        return dictDataRepository.findByDictTypeAndStatusAndDeletedFalseOrderByDictSortAsc(
                dictType, EnableStatus.ENABLED
            )
            .stream()
            .map(this::toDataResp)
            .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.DICT_TYPE, allEntries = true),
        @CacheEvict(value = CacheConfig.DICT_DATA, allEntries = true)
    })
    public void refreshCache() {
        log.info("字典缓存已刷新");
    }

    @Override
    @Cacheable(value = CacheConfig.DICT_DATA, key = "'label:' + #dictType + ':' + #dictValue")
    public String getDictLabel(String dictType, String dictValue) {
        return getDictDataByType(dictType).stream()
            .filter(d -> d.dictValue().equals(dictValue))
            .map(DictDataResp::dictLabel)
            .findFirst()
            .orElse(dictValue);
    }

    @Override
    @Cacheable(value = CacheConfig.DICT_DATA, key = "'value:' + #dictType + ':' + #dictLabel")
    public String getDictValue(String dictType, String dictLabel) {
        return getDictDataByType(dictType).stream()
            .filter(d -> d.dictLabel().equals(dictLabel))
            .map(DictDataResp::dictValue)
            .findFirst()
            .orElse(dictLabel);
    }

    // ========== 转换方法 ==========

    private DictTypeResp toTypeResp(DictType entity) {
        return new DictTypeResp(
            entity.getId(),
            entity.getDictName(),
            entity.getDictType(),
            entity.getStatus(),
            entity.getRemark(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private DictDataResp toDataResp(DictData entity) {
        return new DictDataResp(
            entity.getId(),
            entity.getDictType(),
            entity.getDictLabel(),
            entity.getDictValue(),
            entity.getDictSort(),
            entity.getCssClass(),
            entity.getListClass(),
            entity.getIsDefault(),
            entity.getStatus(),
            entity.getRemark(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
