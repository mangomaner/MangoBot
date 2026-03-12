package io.github.mangomaner.mangobot.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelProvider;
import io.github.mangomaner.mangobot.configuration.model.dto.model.CreateModelProviderRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelProviderRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelProviderVO;

import java.util.List;

/**
 * 模型供应商服务接口
 */
public interface ModelProviderService extends IService<ModelProvider> {

    /**
     * 获取所有供应商
     */
    List<ModelProviderVO> getAllProviders();

    /**
     * 根据ID获取供应商
     */
    ModelProviderVO getProviderById(Long id);

    /**
     * 根据名称获取供应商
     */
    ModelProviderVO getProviderByName(String name);

    /**
     * 创建供应商
     */
    ModelProviderVO createProvider(CreateModelProviderRequest request);

    /**
     * 更新供应商
     */
    ModelProviderVO updateProvider(UpdateModelProviderRequest request);

    /**
     * 删除供应商
     */
    boolean deleteProvider(Long id);

    /**
     * 测试供应商连接
     */
    boolean testConnection(Long id);
}
