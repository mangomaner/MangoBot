package io.github.mangomaner.mangobot;

import io.github.mangomaner.mangobot.module.agent.capability.mcp.McpConnectionManager;
import io.github.mangomaner.mangobot.module.agent.capability.skill.SkillManager;
import io.github.mangomaner.mangobot.module.agent.capability.tool.ToolRegistrationService;
import io.github.mangomaner.mangobot.module.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.api.MangoApiManager;
import io.github.mangomaner.mangobot.plugin.core.PluginManager;
import io.github.mangomaner.mangobot.adapter.onebot.service.OneBotConfigService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MangoBot 主启动类
 * 
 * 应用生命周期：
 * 1. Spring Bean 加载阶段：创建所有 Bean，不执行业务初始化
 * 2. CommandLineRunner 阶段：数据库初始化（自动执行）
 * 3. 手动初始化阶段：
 *    - API 初始化
 *    - 模型初始化
 *    - 内置工具注册
 *    - Skill 同步初始化
 *    - MCP 连接初始化
 * 4. 插件加载阶段：加载并启动所有已启用的插件
 * 5. WebSocket 服务器启动阶段：启动所有已启用的 WebSocket 服务器
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan("io.github.mangomaner.mangobot.system.mapper")
@Slf4j
public class MangoBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MangoBotApplication.class, args);

        log.info("========== MangoBot 初始化开始 ==========");

        MangoApiManager apiManager = context.getBean(MangoApiManager.class);
        ModelProvider modelProvider = context.getBean(ModelProvider.class);
        PluginManager pluginManager = context.getBean(PluginManager.class);
        SkillManager skillManager = context.getBean(SkillManager.class);
        McpConnectionManager mcpConnectionManager = context.getBean(McpConnectionManager.class);
        ToolRegistrationService toolRegistrationService = context.getBean(ToolRegistrationService.class);

        apiManager.init();
        log.info("[1/7] 静态 API 初始化完成");

        modelProvider.init();
        log.info("[2/7] 模型初始化完成");

        toolRegistrationService.initBuiltInTools();
        log.info("[3/7] 内置工具注册完成");

        skillManager.init();
        log.info("[4/7] Skill 同步完成");

        mcpConnectionManager.init();
        log.info("[5/7] MCP 连接初始化完成");

        pluginManager.init();
        log.info("[6/7] 插件加载完成");

        OneBotConfigService oneBotConfigService = context.getBean(OneBotConfigService.class);
        oneBotConfigService.startAllEnabledServers();
        log.info("[7/7] 已启用的 WebSocket 服务器启动完成");

        log.info("控制台请访问：http://localhost:8082/static/mangobot/");

        log.info("========== MangoBot 启动成功 ==========");

    }
}
