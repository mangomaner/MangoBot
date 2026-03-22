package io.github.mangomaner.mangobot.plugin.example;

import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.adapter.onebot.model.segment.TextSegment;
import io.github.mangomaner.mangobot.annotation.ConfigMeta;
import io.github.mangomaner.mangobot.annotation.InjectConfig;
import io.github.mangomaner.mangobot.annotation.PluginDescribe;
import io.github.mangomaner.mangobot.annotation.PluginPriority;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.annotation.web.MangoBotPathVariable;
import io.github.mangomaner.mangobot.annotation.web.MangoBotRequestBody;
import io.github.mangomaner.mangobot.annotation.web.MangoBotRequestMapping;
import io.github.mangomaner.mangobot.annotation.web.MangoBotRequestParam;
import io.github.mangomaner.mangobot.annotation.web.MangoRequestMethod;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.MangoToolApi;
import io.github.mangomaner.mangobot.events.configuration.PluginConfigChangedEvent;
import io.github.mangomaner.mangobot.events.onebot.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.events.onebot.message.OneBotPrivateMessageEvent;
import io.github.mangomaner.mangobot.module.configuration.enums.ConfigType;
import io.github.mangomaner.mangobot.plugin.Plugin;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@MangoBotRequestMapping("/example")
@PluginDescribe(
    name = "ExamplePlugin",
    author = "mangomaner",
    version = "1.0.0",
    description = "MangoBot 示例插件，演示插件系统的完整功能，包括自定义工具注册",
    enableWeb = true
)
public class ExamplePlugin implements Plugin {

    @InjectConfig(
        key = "plugin.example.enabled",
        defaultValue = "true",
        type = ConfigType.BOOLEAN,
        description = "是否启用自动回复功能",
        explain = "开启后，机器人会自动回复特定消息",
        category = "basic",
        metadata = @ConfigMeta(placeholder = "开启/关闭自动回复")
    )
    private Boolean autoReplyEnabled;

    @InjectConfig(
        key = "plugin.example.replyPrefix",
        defaultValue = "[示例插件]",
        type = ConfigType.STRING,
        description = "回复消息前缀",
        explain = "机器人回复消息时添加的前缀",
        category = "basic",
        metadata = @ConfigMeta(placeholder = "请输入前缀", maxLength = 20)
    )
    private String replyPrefix;

    @InjectConfig(
        key = "plugin.example.maxReplyLength",
        defaultValue = "500",
        type = ConfigType.INTEGER,
        description = "最大回复长度",
        explain = "机器人回复消息的最大字符数",
        category = "advanced",
        metadata = @ConfigMeta(min = 1, max = 2000, placeholder = "1-2000")
    )
    private Integer maxReplyLength;

    @InjectConfig(
        key = "plugin.example.apiTimeout",
        defaultValue = "30",
        type = ConfigType.INTEGER,
        description = "API超时时间",
        explain = "请求外部API的超时时间，单位秒",
        category = "network",
        metadata = @ConfigMeta(min = 5, max = 120, placeholder = "5-120秒")
    )
    private Integer apiTimeout;

    @Override
    public void onEnable() {
        log.info("ExamplePlugin 已启用，当前配置：autoReplyEnabled={}, replyPrefix={}, maxReplyLength={}, apiTimeout={}",
            autoReplyEnabled, replyPrefix, maxReplyLength, apiTimeout);
        
        Integer weatherToolId = MangoToolApi.registerTool(WeatherTool.class);
        log.info("WeatherTool 注册成功，工具ID: {}", weatherToolId);
        
        Integer calcToolId = MangoToolApi.registerTool(CalculatorTool.class, 2, true);
        log.info("CalculatorTool 注册成功（精度=2，启用历史），工具ID: {}", calcToolId);
    }

    @Override
    public void onDisable() {
        MangoToolApi.unregisterTool(WeatherTool.class);
        MangoToolApi.unregisterTool(CalculatorTool.class);
        log.info("ExamplePlugin 已禁用，所有工具已注销");
    }

    @MangoBotEventListener
    @PluginPriority(5)
    public boolean onGroupMessage(OneBotGroupMessageEvent event) {
        String message = event.getRawMessage();
        long userId = event.getUserId();
        long groupId = event.getGroupId();

        log.info("收到群消息 [群:{}] [用户:{}]: {}", groupId, userId, message);

        if (!Boolean.TRUE.equals(autoReplyEnabled)) {
            return false;
        }

        String trimmedMessage = message.trim();

        if ("#example help".equalsIgnoreCase(trimmedMessage)) {
            sendGroupReply(event.getSelfId(), groupId,
                "示例插件帮助：\n" +
                "#example help - 显示帮助\n" +
                "#example ping - 测试回复\n" +
                "#example info - 显示插件信息\n" +
                "#example weather <城市> - 获取天气预报（演示工具）");
            return true;
        }

        if ("#example ping".equalsIgnoreCase(trimmedMessage)) {
            sendGroupReply(event.getSelfId(), groupId, "pong! 插件运行正常");
            return true;
        }

        if ("#example info".equalsIgnoreCase(trimmedMessage)) {
            sendGroupReply(event.getSelfId(), groupId,
                String.format("插件信息：\n名称: ExamplePlugin\n版本: 1.0.0\n作者: mangomaner\n自动回复: %s\n最大回复长度: %d",
                    autoReplyEnabled, maxReplyLength));
            return true;
        }

        if (trimmedMessage.toLowerCase().startsWith("#example weather ")) {
            String city = trimmedMessage.substring("#example weather ".length()).trim();
            if (!city.isEmpty()) {
                WeatherTool weatherTool = new WeatherTool();
                String weather = weatherTool.getWeather(city);
                sendGroupReply(event.getSelfId(), groupId, weather);
                return true;
            }
        }

        return false;
    }

    @MangoBotEventListener
    @PluginPriority(5)
    public boolean onPrivateMessage(OneBotPrivateMessageEvent event) {
        String message = event.getRawMessage();
        long userId = event.getUserId();

        log.info("收到私聊消息 [用户:{}]: {}", userId, message);

        if (message.contains("你好") || message.contains("hello")) {
            sendPrivateReply(event.getSelfId(), userId,
                replyPrefix + " 你好！我是 ExamplePlugin，很高兴为你服务！");
            return true;
        }

        return false;
    }

    @MangoBotEventListener
    public boolean onPluginConfigChanged(PluginConfigChangedEvent event) {
        log.info("插件配置变更: configKey={}, newValue={}", event.getConfigKey(), event.getNewValue());

        if (event.getConfigKey().endsWith("enabled")) {
            this.autoReplyEnabled = Boolean.parseBoolean(event.getNewValue());
        } else if (event.getConfigKey().endsWith("replyPrefix")) {
            this.replyPrefix = event.getNewValue();
        } else if (event.getConfigKey().endsWith("maxReplyLength")) {
            this.maxReplyLength = Integer.parseInt(event.getNewValue());
        } else if (event.getConfigKey().endsWith("apiTimeout")) {
            this.apiTimeout = Integer.parseInt(event.getNewValue());
        }

        return true;
    }

    private void sendGroupReply(long botId, long groupId, String text) {
        try {
            OneBotSendingMessage message = new OneBotSendingMessage();
            TextSegment textSegment = new TextSegment();
            TextSegment.TextData textData = new TextSegment.TextData();
            textData.setText(text);
            textSegment.setData(textData);
            message.setMessage(List.of(textSegment));
            MangoOneBotApi.sendGroupMsg(botId, groupId, message);
        } catch (Exception e) {
            log.error("发送群消息失败", e);
        }
    }

    private void sendPrivateReply(long botId, long userId, String text) {
        try {
            OneBotSendingMessage message = new OneBotSendingMessage();
            TextSegment textSegment = new TextSegment();
            TextSegment.TextData textData = new TextSegment.TextData();
            textData.setText(text);
            textSegment.setData(textData);
            message.setMessage(List.of(textSegment));
            MangoOneBotApi.sendPrivateMsg(botId, userId, message);
        } catch (Exception e) {
            log.error("发送私聊消息失败", e);
        }
    }

    @MangoBotRequestMapping(value = "/hello", method = MangoRequestMethod.GET)
    public String hello() {
        return "Hello from ExamplePlugin!";
    }

    @MangoBotRequestMapping(value = "/greet/{name}", method = MangoRequestMethod.GET)
    public String greet(@MangoBotPathVariable("name") String name) {
        return String.format("你好, %s! 欢迎使用 ExamplePlugin", name);
    }

    @MangoBotRequestMapping(value = "/echo", method = MangoRequestMethod.GET)
    public String echo(@MangoBotRequestParam(value = "message", defaultValue = "Hello") String message) {
        return String.format("Echo: %s", message);
    }

    @MangoBotRequestMapping(value = "/data", method = MangoRequestMethod.POST)
    public String saveData(@MangoBotRequestBody PluginData data) {
        log.info("收到数据: id={}, name={}, value={}", data.getId(), data.getName(), data.getValue());
        return String.format("数据已接收: id=%d, name=%s", data.getId(), data.getName());
    }

    @MangoBotRequestMapping(value = "/config", method = MangoRequestMethod.GET)
    public PluginConfigInfo getConfig() {
        return new PluginConfigInfo(autoReplyEnabled, replyPrefix, maxReplyLength, apiTimeout);
    }

    public static class PluginData {
        private Integer id;
        private String name;
        private String value;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class PluginConfigInfo {
        private final Boolean autoReplyEnabled;
        private final String replyPrefix;
        private final Integer maxReplyLength;
        private final Integer apiTimeout;

        public PluginConfigInfo(Boolean autoReplyEnabled, String replyPrefix, Integer maxReplyLength, Integer apiTimeout) {
            this.autoReplyEnabled = autoReplyEnabled;
            this.replyPrefix = replyPrefix;
            this.maxReplyLength = maxReplyLength;
            this.apiTimeout = apiTimeout;
        }

        public Boolean getAutoReplyEnabled() { return autoReplyEnabled; }
        public String getReplyPrefix() { return replyPrefix; }
        public Integer getMaxReplyLength() { return maxReplyLength; }
        public Integer getApiTimeout() { return apiTimeout; }
    }
}
