package io.github.mangomaner.mangobot.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.configuration.event.ModelRoleChangedEvent;
import io.github.mangomaner.mangobot.mapper.configuration.ModelRoleMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelConfig;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelProvider;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelRole;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelRoleVO;
import io.github.mangomaner.mangobot.manager.event.MangoEventPublisher;
import io.github.mangomaner.mangobot.mapper.configuration.ModelConfigMapper;
import io.github.mangomaner.mangobot.mapper.configuration.ModelProviderMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模型提供者实现
 * 统一管理模型实例，支持角色与模型的动态映射
 * 实现 ModelProvider 接口，对外提供模型实例
 */
@Service
@Slf4j
@MangoBotEventListener
public class ModelProviderImpl implements io.github.mangomaner.mangobot.configuration.service.ModelProvider {

    private final Map<String, OpenAIChatModel> modelCache = new ConcurrentHashMap<>();

    @Resource
    private ModelRoleMapper modelRoleMapper;

    @Resource
    private ModelConfigMapper modelConfigMapper;

    @Resource
    private ModelProviderMapper modelProviderMapper;

    @Resource
    @Lazy
    private MangoEventPublisher mangoEventPublisher;

    /**
     * 初始化模型提供者
     * 加载所有已配置的模型角色
     */
    public void init() {
        log.info("初始化模型提供者...");
        List<ModelRole> roles = modelRoleMapper.selectList(null);
        for (ModelRole role : roles) {
            if (role.getModelConfigId() != null) {
                try {
                    loadModel(role.getRoleKey(), role.getModelConfigId());
                } catch (Exception e) {
                    log.error("加载角色 {} 的模型失败", role.getRoleKey(), e);
                }
            }
        }
        log.info("模型提供者初始化完成，已加载 {} 个模型", modelCache.size());
    }

    @MangoBotEventListener
    public boolean onModelRoleChanged(ModelRoleChangedEvent event) {
        String roleKey = event.getRoleKey();
        log.info("收到模型角色变更事件: {}", roleKey);

        if (event.getNewModelConfigId() != null) {
            loadModel(roleKey, event.getNewModelConfigId());
        } else {
            modelCache.remove(roleKey);
            log.info("角色 {} 的模型已移除", roleKey);
        }
        return true;
    }

    @Override
    public OpenAIChatModel getModel(String roleKey) {
        OpenAIChatModel model = modelCache.get(roleKey);
        if (model == null) {
            Long configId = getModelConfigIdByKey(roleKey);
            if (configId != null) {
                loadModel(roleKey, configId);
                model = modelCache.get(roleKey);
            }
        }
        return model;
    }

    @Override
    public ModelConfigVO getModelConfig(String roleKey) {
        Long configId = getModelConfigIdByKey(roleKey);
        if (configId == null) {
            return null;
        }

        ModelConfig config = modelConfigMapper.selectById(configId);
        if (config == null) {
            return null;
        }

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

    @Override
    public void refreshModel(String roleKey) {
        Long configId = getModelConfigIdByKey(roleKey);
        if (configId != null) {
            loadModel(roleKey, configId);
        } else {
            modelCache.remove(roleKey);
        }
    }

    @Override
    public List<ModelRoleVO> getAllRoles() {
        List<ModelRole> roles = modelRoleMapper.selectList(null);
        return roles.stream()
                .map(this::convertToRoleVO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelRoleVO getRole(String roleKey) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = modelRoleMapper.selectOne(wrapper);
        return role != null ? convertToRoleVO(role) : null;
    }

    @Override
    public void updateRoleModel(String roleKey, Long modelConfigId) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = modelRoleMapper.selectOne(wrapper);

        if (role == null) {
            log.warn("角色 {} 不存在", roleKey);
            return;
        }

        Long oldConfigId = role.getModelConfigId();
        role.setModelConfigId(modelConfigId);
        role.setUpdatedAt(System.currentTimeMillis());
        modelRoleMapper.updateById(role);

        mangoEventPublisher.publish(new ModelRoleChangedEvent(
                role.getId(),
                roleKey,
                oldConfigId,
                modelConfigId
        ));

        log.info("更新角色 {} 的模型配置: {} -> {}", roleKey, oldConfigId, modelConfigId);
    }

    private void loadModel(String roleKey, Long configId) {
        ModelConfig config = modelConfigMapper.selectById(configId);
        if (config == null || config.getIsEnabled() != 1) {
            log.warn("模型配置 {} 不存在或已禁用", configId);
            modelCache.remove(roleKey);
            return;
        }

        ModelProvider provider = modelProviderMapper.selectById(config.getProviderId());
        if (provider == null || provider.getIsEnabled() != 1) {
            log.warn("供应商 {} 不存在或已禁用", config.getProviderId());
            modelCache.remove(roleKey);
            return;
        }

        OpenAIChatModel model = OpenAIChatModel.builder()
                .baseUrl(provider.getBaseUrl())
                .apiKey(provider.getApiKey())
                .modelName(config.getModelName())
                .build();

        modelCache.put(roleKey, model);
        log.info("加载模型成功: {} -> {} ({})", roleKey, config.getModelName(), provider.getName());
    }

    private Long getModelConfigIdByKey(String roleKey) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = modelRoleMapper.selectOne(wrapper);
        return role != null ? role.getModelConfigId() : null;
    }

    private ModelRoleVO convertToRoleVO(ModelRole role) {
        ModelRoleVO vo = new ModelRoleVO();
        vo.setId(role.getId());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleName(role.getRoleName());
        vo.setModelConfigId(role.getModelConfigId());
        vo.setDescription(role.getDescription());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());

        if (role.getModelConfigId() != null) {
            ModelConfig config = modelConfigMapper.selectById(role.getModelConfigId());
            if (config != null) {
                vo.setModelName(config.getModelName());
                ModelProvider provider = modelProviderMapper.selectById(config.getProviderId());
                if (provider != null) {
                    vo.setProviderName(provider.getName());
                }
            }
        }

        return vo;
    }
}
