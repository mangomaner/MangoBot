package io.github.mangomaner.mangobot.module.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.module.configuration.event.ModelRoleChangedEvent;
import io.github.mangomaner.mangobot.system.mapper.configuration.ModelRoleMapper;
import io.github.mangomaner.mangobot.module.configuration.model.domain.ModelRole;
import io.github.mangomaner.mangobot.module.configuration.model.dto.model.UpdateModelRoleRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.ModelRoleVO;
import io.github.mangomaner.mangobot.module.configuration.service.ModelRoleService;
import io.github.mangomaner.mangobot.manager.event.MangoEventPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型角色服务实现
 */
@Service
@Slf4j
public class ModelRoleServiceImpl extends ServiceImpl<ModelRoleMapper, ModelRole>
        implements ModelRoleService {

    @Resource
    @Lazy
    private MangoEventPublisher mangoEventPublisher;

    @Override
    public List<ModelRoleVO> getAllRoles() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelRoleVO getRoleByKey(String roleKey) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = this.getOne(wrapper);
        return role != null ? convertToVO(role) : null;
    }

    @Override
    public Long getModelConfigIdByKey(String roleKey) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = this.getOne(wrapper);
        return role != null ? role.getModelConfigId() : null;
    }

    @Override
    public ModelRoleVO updateRoleModel(String roleKey, UpdateModelRoleRequest request) {
        LambdaQueryWrapper<ModelRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelRole::getRoleKey, roleKey);
        ModelRole role = this.getOne(wrapper);

        if (role == null) {
            log.warn("角色 {} 不存在", roleKey);
            return null;
        }

        Long oldConfigId = role.getModelConfigId();
        role.setModelConfigId(request.getModelConfigId());
        role.setUpdatedAt(System.currentTimeMillis());
        this.updateById(role);

        mangoEventPublisher.publish(new ModelRoleChangedEvent(
                role.getId(),
                roleKey,
                oldConfigId,
                request.getModelConfigId()
        ));

        log.info("更新角色 {} 的模型配置: {} -> {}", roleKey, oldConfigId, request.getModelConfigId());
        return convertToVO(role);
    }

    private ModelRoleVO convertToVO(ModelRole role) {
        ModelRoleVO vo = new ModelRoleVO();
        vo.setId(role.getId());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleName(role.getRoleName());
        vo.setModelConfigId(role.getModelConfigId());
        vo.setDescription(role.getDescription());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        return vo;
    }
}
