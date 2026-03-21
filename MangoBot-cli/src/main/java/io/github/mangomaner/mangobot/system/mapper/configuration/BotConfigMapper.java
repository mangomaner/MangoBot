package io.github.mangomaner.mangobot.system.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.module.configuration.model.domain.BotConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * Bot 配置 Mapper 接口
 */
@Mapper
public interface BotConfigMapper extends BaseMapper<BotConfig> {
}
