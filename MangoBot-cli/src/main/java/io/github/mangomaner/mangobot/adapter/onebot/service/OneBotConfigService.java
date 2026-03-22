package io.github.mangomaner.mangobot.adapter.onebot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.adapter.onebot.model.domain.OneBotConfig;
import io.github.mangomaner.mangobot.adapter.onebot.model.dto.CreateOneBotConfigRequest;
import io.github.mangomaner.mangobot.adapter.onebot.model.dto.UpdateOneBotConfigRequest;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.OneBotConfigVO;

import java.util.List;

public interface OneBotConfigService extends IService<OneBotConfig> {

    List<OneBotConfigVO> listAll();

    OneBotConfigVO getById(Long id);

    Long createConfig(CreateOneBotConfigRequest request);

    void updateConfig(UpdateOneBotConfigRequest request);

    void deleteConfig(Long id);

    void startServer(Long id);

    void stopServer(Long id);

    void setEnabled(Long id, boolean enabled);

    OneBotConfigVO getServerStatus(Long id);

    List<OneBotConfig> listEnabled();

    void startAllEnabledServers();
}
