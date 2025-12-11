/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.dict.*;

import java.util.List;
import java.util.UUID;

/**
 * 字典服务接口
 */
public interface DictService {

    // ========== 字典类型操作 ==========

    /**
     * 创建字典类型
     */
    UUID createDictType(DictTypeCreateReq req);

    /**
     * 更新字典类型
     */
    void updateDictType(DictTypeUpdateReq req);

    /**
     * 删除字典类型（逻辑删除）
     */
    void deleteDictType(UUID dictTypeId);

    /**
     * 根据ID查询字典类型
     */
    DictTypeResp getDictTypeById(UUID dictTypeId);

    /**
     * 查询所有字典类型列表
     */
    List<DictTypeResp> getAllDictTypes();

    /**
     * 检查字典类型是否存在
     */
    boolean existsByDictType(String dictType);

    // ========== 字典数据操作 ==========

    /**
     * 创建字典数据
     */
    UUID createDictData(DictDataCreateReq req);

    /**
     * 更新字典数据
     */
    void updateDictData(DictDataUpdateReq req);

    /**
     * 删除字典数据（逻辑删除）
     */
    void deleteDictData(UUID dictDataId);

    /**
     * 根据ID查询字典数据
     */
    DictDataResp getDictDataById(UUID dictDataId);

    /**
     * 根据字典类型查询字典数据列表
     */
    List<DictDataResp> getDictDataByType(String dictType);

    /**
     * 刷新字典缓存
     */
    void refreshCache();

    /**
     * 根据字典类型和字典值获取标签
     */
    String getDictLabel(String dictType, String dictValue);

    /**
     * 根据字典类型和字典标签获取值
     */
    String getDictValue(String dictType, String dictLabel);
}
