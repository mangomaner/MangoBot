package io.github.mangomaner.mangobot.events.configuration;

import io.github.mangomaner.mangobot.module.configuration.event.ConfigurationEvent;
import lombok.Getter;

/**
 * 模型角色变更事件
 * 当模型角色与模型配置的映射关系发生变更时发布此事件
 */
@Getter
public class ModelRoleChangedEvent extends ConfigurationEvent {

    private final Long roleId;

    private final String roleKey;

    private final Long oldModelConfigId;

    private final Long newModelConfigId;

    public ModelRoleChangedEvent(Long roleId, String roleKey, 
                                  Long oldModelConfigId, Long newModelConfigId) {
        super(roleKey, 
              oldModelConfigId != null ? oldModelConfigId.toString() : null,
              newModelConfigId != null ? newModelConfigId.toString() : null);
        this.roleId = roleId;
        this.roleKey = roleKey;
        this.oldModelConfigId = oldModelConfigId;
        this.newModelConfigId = newModelConfigId;
    }

    @Override
    public String getPostType() {
        return "model_role_event";
    }
}
