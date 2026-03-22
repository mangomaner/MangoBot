package io.github.mangomaner.mangobot.system.mapper.connection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.mangomaner.mangobot.adapter.onebot.model.domain.OneBotConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OneBotConfigMapper extends BaseMapper<OneBotConfig> {
}
