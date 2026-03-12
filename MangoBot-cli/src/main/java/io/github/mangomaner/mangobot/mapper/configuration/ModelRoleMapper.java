package io.github.mangomaner.mangobot.mapper.configuration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.ModelRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型角色 Mapper 接口
 */
@Mapper
public interface ModelRoleMapper extends BaseMapper<ModelRole> {
}
