package io.github.mangomaner.mangobot.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型配置 Mapper 接口
 */
@Mapper
public interface ModelConfigMapper extends BaseMapper<ModelConfig> {
}
