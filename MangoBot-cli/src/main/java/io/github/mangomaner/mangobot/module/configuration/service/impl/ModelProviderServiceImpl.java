package io.github.mangomaner.mangobot.module.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.module.configuration.model.domain.ModelProvider;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.CreateModelProviderRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.UpdateModelProviderRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelProviderVO;
import io.github.mangomaner.mangobot.module.configuration.service.ModelProviderService;
import io.github.mangomaner.mangobot.system.mapper.configuration.ModelProviderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型供应商服务实现
 */
@Service
@Slf4j
public class ModelProviderServiceImpl extends ServiceImpl<ModelProviderMapper, ModelProvider>
        implements ModelProviderService {

    @Override
    public List<ModelProviderVO> getAllProviders() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelProviderVO getProviderById(Long id) {
        ModelProvider provider = this.getById(id);
        return provider != null ? convertToVO(provider) : null;
    }

    @Override
    public ModelProviderVO getProviderByName(String name) {
        LambdaQueryWrapper<ModelProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelProvider::getName, name);
        ModelProvider provider = this.getOne(wrapper);
        return provider != null ? convertToVO(provider) : null;
    }

    @Override
    public ModelProviderVO createProvider(CreateModelProviderRequest request) {
        ModelProvider provider = new ModelProvider();
        provider.setName(request.getName());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setApiKey(request.getApiKey());
        provider.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30);
        provider.setDescription(request.getDescription());
        provider.setIsEnabled(1);
        this.save(provider);
        log.info("创建模型供应商成功: {}", provider.getName());
        return convertToVO(provider);
    }

    @Override
    public ModelProviderVO updateProvider(UpdateModelProviderRequest request) {
        ModelProvider provider = this.getById(request.getId());
        if (provider == null) {
            return null;
        }
        if (request.getName() != null) {
            provider.setName(request.getName());
        }
        if (request.getBaseUrl() != null) {
            provider.setBaseUrl(request.getBaseUrl());
        }
        if (request.getApiKey() != null) {
            provider.setApiKey(request.getApiKey());
        }
        if (request.getTimeout() != null) {
            provider.setTimeout(request.getTimeout());
        }
        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription());
        }
        if (request.getIsEnabled() != null) {
            provider.setIsEnabled(request.getIsEnabled() ? 1 : 0);
        }
        provider.setUpdatedAt(System.currentTimeMillis());
        this.updateById(provider);
        log.info("更新模型供应商成功: {}", provider.getName());
        return convertToVO(provider);
    }

    @Override
    public boolean deleteProvider(Long id) {
        ModelProvider provider = this.getById(id);
        if (provider == null) {
            return false;
        }
        boolean result = this.removeById(id);
        if (result) {
            log.info("删除模型供应商成功: {}", provider.getName());
        }
        return result;
    }

    @Override
    public boolean testConnection(Long id) {
        ModelProvider provider = this.getById(id);
        if (provider == null || provider.getIsEnabled() != 1) {
            return false;
        }
        return provider.getApiKey() != null && !provider.getApiKey().isEmpty();
    }

    private ModelProviderVO convertToVO(ModelProvider provider) {
        ModelProviderVO vo = new ModelProviderVO();
        vo.setId(provider.getId());
        vo.setName(provider.getName());
        vo.setBaseUrl(provider.getBaseUrl());
        vo.setApiKey(maskApiKey(provider.getApiKey()));
        vo.setTimeout(provider.getTimeout());
        vo.setDescription(provider.getDescription());
        vo.setIsEnabled(provider.getIsEnabled() == 1);
        vo.setCreatedAt(provider.getCreatedAt());
        vo.setUpdatedAt(provider.getUpdatedAt());
        return vo;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
