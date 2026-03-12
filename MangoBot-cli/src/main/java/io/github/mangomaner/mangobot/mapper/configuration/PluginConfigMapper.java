package io.github.mangomaner.mangobot.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.PluginConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 插件配置 Mapper 接口
 */
@Mapper
public interface PluginConfigMapper extends BaseMapper<PluginConfigEntity> {
}
