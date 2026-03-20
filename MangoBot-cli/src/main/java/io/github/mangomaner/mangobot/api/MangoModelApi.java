package io.github.mangomaner.mangobot.api;

import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.api.enums.ModelRole;
import io.github.mangomaner.mangobot.module.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelRoleVO;

import java.util.List;

/**
 * 模型 API (静态工具类)
 * 提供对 AI 模型的访问能力，支持按角色获取模型实例。
 */
public class MangoModelApi {

    private static ModelProvider provider;

    private MangoModelApi() {}

    static void setProvider(ModelProvider provider) {
        MangoModelApi.provider = provider;
    }

    private static void checkProvider() {
        if (provider == null) {
            throw new IllegalStateException("MangoModelApi has not been initialized yet.");
        }
    }

    /**
     * 获取指定角色的模型实例
     * @param role 模型角色枚举
     * @return 模型实例，如果未配置则返回 null
     */
    public static OpenAIChatModel getModel(ModelRole role) {
        checkProvider();
        return provider.getModel(role.getRoleKey());
    }

    /**
     * 获取指定角色的模型实例
     * @param roleKey 角色标识（main, assistant, image, embedding）
     * @return 模型实例，如果未配置则返回 null
     */
    public static OpenAIChatModel getModel(String roleKey) {
        checkProvider();
        return provider.getModel(roleKey);
    }

    /**
     * 获取指定角色的模型配置详情
     * @param role 模型角色枚举
     * @return 模型配置详情
     */
    public static ModelConfigVO getModelConfig(ModelRole role) {
        checkProvider();
        return provider.getModelConfig(role.getRoleKey());
    }

    /**
     * 获取指定角色的模型配置详情
     * @param roleKey 角色标识
     * @return 模型配置详情
     */
    public static ModelConfigVO getModelConfig(String roleKey) {
        checkProvider();
        return provider.getModelConfig(roleKey);
    }

    /**
     * 获取所有角色配置
     * @return 角色配置列表
     */
    public static List<ModelRoleVO> getAllRoles() {
        checkProvider();
        return provider.getAllRoles();
    }

    /**
     * 获取指定角色的配置
     * @param role 模型角色枚举
     * @return 角色配置
     */
    public static ModelRoleVO getRole(ModelRole role) {
        checkProvider();
        return provider.getRole(role.getRoleKey());
    }

    /**
     * 获取指定角色的配置
     * @param roleKey 角色标识
     * @return 角色配置
     */
    public static ModelRoleVO getRole(String roleKey) {
        checkProvider();
        return provider.getRole(roleKey);
    }

}
