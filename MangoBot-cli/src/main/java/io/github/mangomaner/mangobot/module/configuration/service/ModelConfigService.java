package io.github.mangomaner.mangobot.module.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.module.configuration.model.domain.ModelConfig;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.CreateModelConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.TestModelRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.UpdateModelConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelTestResultVO;

import java.util.List;

/**
 * 模型配置服务接口
 */
public interface ModelConfigService extends IService<ModelConfig> {

    /**
     * 获取所有模型配置
     */
    List<ModelConfigVO> getAllConfigs();

    /**
     * 根据ID获取模型配置
     */
    ModelConfigVO getConfigById(Long id);

    /**
     * 创建模型配置
     */
    ModelConfigVO createConfig(CreateModelConfigRequest request);

    /**
     * 更新模型配置
     */
    ModelConfigVO updateConfig(UpdateModelConfigRequest request);

    /**
     * 删除模型配置
     */
    boolean deleteConfig(Long id);

    /**
     * 测试模型
     */
    ModelTestResultVO testModel(Long id, TestModelRequest request);
}
