package io.github.mangomaner.mangobot.api;

import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelRoleVO;

import java.util.List;

/**
 * 模型 API (静态工具类)
 * 提供对 AI 模型的访问能力，支持按角色获取模型实例。
 */
public class MangoModelApi {

    // 由io.github.mangomaner.mangobot.manager.MangoApiManager反射注入
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
     * 获取主模型
     * @return 主模型实例
     */
    public static OpenAIChatModel getMainModel() {
        checkProvider();
        return provider.getModel("main");
    }

    /**
     * 获取助手模型
     * @return 助手模型实例
     */
    public static OpenAIChatModel getAssistantModel() {
        checkProvider();
        return provider.getModel("assistant");
    }

    /**
     * 获取图片模型
     * @return 图片模型实例
     */
    public static OpenAIChatModel getImageModel() {
        checkProvider();
        return provider.getModel("image");
    }

    /**
     * 获取向量模型
     * @return 向量模型实例
     */
    public static OpenAIChatModel getEmbeddingModel() {
        checkProvider();
        return provider.getModel("embedding");
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
     * @param roleKey 角色标识
     * @return 角色配置
     */
    public static ModelRoleVO getRole(String roleKey) {
        checkProvider();
        return provider.getRole(roleKey);
    }

}
