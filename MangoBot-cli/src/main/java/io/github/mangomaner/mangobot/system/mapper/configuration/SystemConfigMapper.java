package io.github.mangomaner.mangobot.system.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.module.configuration.model.domain.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper 接口
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
}
