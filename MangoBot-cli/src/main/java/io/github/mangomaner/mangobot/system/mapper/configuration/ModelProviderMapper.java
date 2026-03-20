package io.github.mangomaner.mangobot.system.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.module.configuration.model.domain.ModelProvider;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型供应商 Mapper 接口
 */
@Mapper
public interface ModelProviderMapper extends BaseMapper<ModelProvider> {
}
