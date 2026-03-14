package io.github.mangomaner.mangobot.configuration.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.common.ErrorCode;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelConfig;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelProvider;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelRole;
import io.github.mangomaner.mangobot.configuration.model.dto.model.CreateModelConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.TestModelRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelTestResultVO;
import io.github.mangomaner.mangobot.configuration.service.ModelConfigService;
import io.github.mangomaner.mangobot.exception.BusinessException;
import io.github.mangomaner.mangobot.mapper.configuration.ModelConfigMapper;
import io.github.mangomaner.mangobot.mapper.configuration.ModelProviderMapper;
import io.github.mangomaner.mangobot.mapper.configuration.ModelRoleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型配置服务实现
 */
@Service
@Slf4j
public class ModelConfigServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig>
        implements ModelConfigService {

    @Resource
    private ModelProviderMapper modelProviderMapper;

    @Resource
    private ModelRoleMapper modelRoleMapper;

    @Resource
    private io.github.mangomaner.mangobot.configuration.core.ModelProvider modelPrivider;

    @Override
    public List<ModelConfigVO> getAllConfigs() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelConfigVO getConfigById(Long id) {
        ModelConfig config = this.getById(id);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    public ModelConfigVO createConfig(CreateModelConfigRequest request) {
        ModelConfig config = new ModelConfig();
        config.setModelName(request.getModelName());
        config.setProviderId(request.getProviderId());
        config.setTemperature(request.getTemperature() != null ? request.getTemperature() : 0.7);
        config.setMaxTokens(request.getMaxTokens());
        config.setTopP(request.getTopP());
        config.setTimeout(request.getTimeout());
        config.setDescription(request.getDescription());
        config.setIsEnabled(1);
        this.save(config);
        log.info("创建模型配置成功: {}", config.getModelName());
        return convertToVO(config);
    }

    @Override
    public ModelConfigVO updateConfig(UpdateModelConfigRequest request) {
        ModelConfig config = this.getById(request.getId());
        if (config == null) {
            return null;
        }

        if (request.getModelName() != null) {
            config.setModelName(request.getModelName());
        }
        if (request.getProviderId() != null) {
            config.setProviderId(request.getProviderId());
        }
        if (request.getTemperature() != null) {
            config.setTemperature(request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            config.setMaxTokens(request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            config.setTopP(request.getTopP());
        }
        if (request.getTimeout() != null) {
            config.setTimeout(request.getTimeout());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getIsEnabled() != null) {
            config.setIsEnabled(request.getIsEnabled() ? 1 : 0);
            if (config.getIsEnabled() == (request.getIsEnabled()? 1 : 0)) {
                List<ModelRole> roles = modelRoleMapper.selectList(new QueryWrapper<ModelRole>().eq("model_config_id", request.getId()));
                if (roles.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (ModelRole role : roles) {
                        sb.append(role.getRoleName()).append(",");
                    }
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "请解除关联：" + sb);
                }
            }
        }
        config.setUpdatedAt(System.currentTimeMillis());

        this.updateById(config);

        // 检查该模型是否被其他角色关联，若关联则向ModelProvider提示更新对应角色的模型
        List<ModelRole> roles = modelRoleMapper.selectList(new QueryWrapper<ModelRole>().eq("model_config_id", request.getId()));
        if (roles.size() > 0) {
            for (ModelRole role : roles) {
                modelPrivider.updateRoleModel(role.getRoleKey(), config.getId());
            }
        }

        log.info("更新模型配置成功: {}", config.getModelName());
        return convertToVO(config);
    }

    @Override
    public boolean deleteConfig(Long id) {
        ModelConfig config = this.getById(id);
        if (config == null) {
            return false;
        }
        // 搜索model_roles表，查看是否有角色关联
        List<ModelRole> roles = modelRoleMapper.selectList(new QueryWrapper<ModelRole>().eq("model_config_id", id));
        if (roles.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ModelRole role : roles) {
                sb.append(role.getRoleName()).append(",");
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请解除关联：" + sb);
        }
        boolean result = this.removeById(id);
        if (result) {
            log.info("删除模型配置成功: {}", config.getModelName());
        }
        return result;
    }

    @Override
    public ModelTestResultVO testModel(Long id, TestModelRequest request) {
        ModelConfig config = this.getById(id);
        if (config == null || config.getIsEnabled() != 1) {
            return ModelTestResultVO.builder()
                    .success(false)
                    .errorMessage("模型配置不存在或已禁用")
                    .build();
        }

        ModelProvider provider = modelProviderMapper.selectById(config.getProviderId());
        if (provider == null || provider.getIsEnabled() != 1) {
            return ModelTestResultVO.builder()
                    .success(false)
                    .errorMessage("供应商不存在或已禁用")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            return ModelTestResultVO.builder()
                    .success(true)
                    .content("模型配置验证成功")
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        } catch (Exception e) {
            log.error("测试模型失败", e);
            return ModelTestResultVO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private ModelConfigVO convertToVO(ModelConfig config) {
        ModelConfigVO vo = new ModelConfigVO();
        vo.setId(config.getId());
        vo.setModelName(config.getModelName());
        vo.setProviderId(config.getProviderId());
        vo.setTemperature(config.getTemperature());
        vo.setMaxTokens(config.getMaxTokens());
        vo.setTopP(config.getTopP());
        vo.setTimeout(config.getTimeout());
        vo.setDescription(config.getDescription());
        vo.setIsEnabled(config.getIsEnabled() == 1);
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());

        ModelProvider provider = modelProviderMapper.selectById(config.getProviderId());
        if (provider != null) {
            vo.setProviderName(provider.getName());
        }
        return vo;
    }
}
