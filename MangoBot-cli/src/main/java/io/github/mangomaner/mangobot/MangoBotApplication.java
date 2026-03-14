package io.github.mangomaner.mangobot;

import io.github.mangomaner.mangobot.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.manager.MangoApiManager;
import io.github.mangomaner.mangobot.plugin.PluginManager;
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
 *    - API 初始化（MangoApiManager）
 *    - 模型初始化（ModelProviderImpl）
 * 4. 插件加载阶段：加载并启动所有已启用的插件
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan("io.github.mangomaner.mangobot.mapper")
@Slf4j
public class MangoBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MangoBotApplication.class, args);

        log.info("========== MangoBot 初始化开始 ==========");

        MangoApiManager apiManager = context.getBean(MangoApiManager.class);
        ModelProvider modelProvider = context.getBean(ModelProvider.class);
        PluginManager pluginManager = context.getBean(PluginManager.class);

        apiManager.init();
        log.info("[1/3] 静态 API 初始化完成");

        modelProvider.init();
        log.info("[2/3] 模型初始化完成");

        pluginManager.init();
        log.info("[3/3] 插件加载完成");

        log.info("========== MangoBot 启动成功 ==========");
    }
}
