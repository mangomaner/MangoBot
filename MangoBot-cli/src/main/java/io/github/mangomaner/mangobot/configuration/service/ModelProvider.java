package io.github.mangomaner.mangobot.configuration.service;

import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelRoleVO;

import java.util.List;

/**
 * 模型提供者接口
 * 统一对外提供模型实例和配置信息
 */
public interface ModelProvider {

    /**
     * 获取指定角色的模型实例
     * @param roleKey 角色标识（main, assistant, image, embedding）
     * @return 模型实例，如果未配置则返回 null
     */
    OpenAIChatModel getModel(String roleKey);

    /**
     * 获取指定角色的模型配置详情
     * @param roleKey 角色标识
     * @return 模型配置详情
     */
    ModelConfigVO getModelConfig(String roleKey);

    /**
     * 刷新指定角色的模型（重新加载）
     * @param roleKey 角色标识
     */
    void refreshModel(String roleKey);

    /**
     * 获取所有角色配置
     * @return 角色配置列表
     */
    List<ModelRoleVO> getAllRoles();

    /**
     * 获取指定角色的配置
     * @param roleKey 角色标识
     * @return 角色配置
     */
    ModelRoleVO getRole(String roleKey);

    /**
     * 更新角色对应的模型
     * @param roleKey 角色标识
     * @param modelConfigId 模型配置ID
     */
    void updateRoleModel(String roleKey, Long modelConfigId);
}
