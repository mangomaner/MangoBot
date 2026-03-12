package io.github.mangomaner.mangobot.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelRole;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelRoleRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelRoleVO;

import java.util.List;

/**
 * 模型角色服务接口
 */
public interface ModelRoleService extends IService<ModelRole> {

    /**
     * 获取所有角色配置
     */
    List<ModelRoleVO> getAllRoles();

    /**
     * 根据角色标识获取配置
     */
    ModelRoleVO getRoleByKey(String roleKey);

    /**
     * 根据角色标识获取关联的模型配置ID
     */
    Long getModelConfigIdByKey(String roleKey);

    /**
     * 更新角色对应的模型
     */
    ModelRoleVO updateRoleModel(String roleKey, UpdateModelRoleRequest request);
}
