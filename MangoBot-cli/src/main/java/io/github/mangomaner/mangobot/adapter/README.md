# Adapter 模块

适配器模块负责处理外部平台连接和消息协议转换，采用**适配器模式**实现多平台协议的统一接入。

## 模块结构

```
adapter/
├── PlatformType.java              # 平台类型枚举
├── WebSocketProtocolAdapter.java  # WebSocket 协议适配器接口
├── connect_controller/            # 连接控制器
│   ├── ConnectionController.java  # 连接管理 API
│   ├── PlatformOptionVO.java      # 平台选项 VO
│   └── onebot/                    # OneBot 协议实现（示例）
└── message_handler/               # 消息处理器
    └── onebot/                    # OneBot 协议处理（示例）
```

## 核心组件

### 平台类型 (PlatformType)

定义支持的平台类型枚举：

```java
@Getter
public enum PlatformType {
    ONEBOT_QQ("onebot_qq", "OneBot QQ", "OneBot 协议 QQ 机器人连接");

    private final String code;
    private final String name;
    private final String description;

    PlatformType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static PlatformType fromCode(String code) { ... }
}
```

### 协议适配器接口 (WebSocketProtocolAdapter)

定义 WebSocket 协议适配器的标准接口：

```java
public interface WebSocketProtocolAdapter {
    String getProtocolType();                                    // 返回协议类型标识
    void onMessage(ConnectionSession session, String message);   // 处理收到的消息
    void onConnect(ConnectionSession session);                   // 连接建立回调
    void onDisconnect(ConnectionSession session);                // 连接断开回调
}
```

---

## 扩展新平台指南

以下步骤说明如何为 MangoBot 添加新的平台协议支持。

### 第一步：添加平台类型枚举

在 [PlatformType.java](PlatformType.java) 中添加新平台：

```java
@Getter
public enum PlatformType {
    ONEBOT_QQ("onebot_qq", "OneBot QQ", "OneBot 协议 QQ 机器人连接"),
    NEW_PLATFORM("new_platform", "新平台", "新平台协议描述");  // 新增

    // ...
}
```

### 第二步：创建目录结构

在 `adapter` 下创建对应的模块：

```
adapter/
├── connect_controller/
│   └── new_platform/                    # 新平台连接控制器
│       ├── controller/
│       │   └── NewPlatformConfigController.java
│       ├── model/
│       │   ├── domain/
│       │   │   └── NewPlatformConfig.java
│       │   ├── dto/
│       │   │   ├── CreateNewPlatformConfigRequest.java
│       │   │   └── UpdateNewPlatformConfigRequest.java
│       │   ├── enums/
│       │   │   └── ConnectionStatus.java
│       │   └── vo/
│       │       └── NewPlatformConfigVO.java
│       └── service/
│           ├── NewPlatformConfigService.java
│           └── impl/
│               └── NewPlatformConfigServiceImpl.java
└── message_handler/
    └── new_platform/                    # 新平台消息处理器
        ├── event/
        │   ├── NewPlatformEvent.java
        │   └── NewPlatformMessageEvent.java
        ├── handler/
        │   └── NewPlatformMessageHandler.java
        ├── inbound/
        │   └── NewPlatformEventParser.java
        └── outbound/
            └── NewPlatformApiService.java
```

### 第三步：实现协议适配器

创建消息处理器实现 `WebSocketProtocolAdapter` 接口：

```java
@Component
public class NewPlatformMessageHandler implements WebSocketProtocolAdapter {

    private static final String PROTOCOL_TYPE = "new_platform";

    private final MangoEventPublisher eventPublisher;
    private final ConnectionSessionManager sessionManager;

    public NewPlatformMessageHandler(MangoEventPublisher eventPublisher,
                                     ConnectionSessionManager sessionManager) {
        this.eventPublisher = eventPublisher;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public void onMessage(ConnectionSession session, String message) {
        // 1. 解析消息
        NewPlatformEvent event = NewPlatformEventParser.parse(message);
        
        // 2. 处理特殊事件（如心跳、生命周期）
        if (event instanceof NewPlatformHeartbeatEvent heartbeat) {
            sessionManager.updateHeartbeat(session, heartbeat.getInterval());
            return;
        }
        
        // 3. 发布事件到事件总线
        eventPublisher.publish(event);
    }

    @Override
    public void onConnect(ConnectionSession session) {
        log.info("新平台连接建立: {}", session.getRemoteAddress());
        sessionManager.registerSession(session);
    }

    @Override
    public void onDisconnect(ConnectionSession session) {
        log.info("新平台连接断开");
        sessionManager.removeSession(session);
    }
}
```

### 第四步：定义事件模型

创建事件接口和具体事件类：

```java
// 事件接口
public interface NewPlatformEvent {
    long getTime();
    String getEventType();
}

// 基础事件（使用 Jackson 多态反序列化）
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event_type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NewPlatformMessageEvent.class, name = "message"),
    @JsonSubTypes.Type(value = NewPlatformHeartbeatEvent.class, name = "heartbeat")
})
@Data
public class NewPlatformBaseEvent implements NewPlatformEvent {
    private long time;
    private String eventType;
}

// 消息事件
@Data
@EqualsAndHashCode(callSuper = true)
public class NewPlatformMessageEvent extends NewPlatformBaseEvent {
    private long senderId;
    private String content;
    private String targetType;  // "group" / "private"
    private long targetId;
}
```

### 第五步：实现 API 服务（可选）

如果平台需要主动调用 API：

```java
@Service
public class NewPlatformApiService {

    private final ConnectionSessionManager sessionManager;

    public void sendMessage(long botId, long targetId, String content) {
        ConnectionSession session = sessionManager.getSessionBySelfId(botId);
        if (session == null || !session.isConnected()) {
            throw new BusinessException("机器人未连接");
        }

        // 构建 API 请求
        Map<String, Object> request = new HashMap<>();
        request.put("action", "send_message");
        request.put("target_id", targetId);
        request.put("content", content);

        // 发送请求
        String json = objectMapper.writeValueAsString(request);
        session.getConnection().send(json);
    }
}
```

### 第六步：创建配置管理

参考 `connect_controller/onebot` 模块实现配置管理：

1. **实体类** - 存储连接配置（host、port、token 等）
2. **Service** - 配置的 CRUD 和服务器生命周期管理
3. **Controller** - REST API 接口

### 第七步：注册事件监听

在插件或业务代码中监听新平台事件：

```java
@Component
public class NewPlatformEventListener {

    @MangoBotEventListener
    public boolean onMessage(NewPlatformMessageEvent event) {
        // 处理消息
        return false;  // 返回 false 继续传递
    }
}
```

---

## 关键依赖

扩展新平台时需要依赖以下组件：

| 组件 | 用途 |
|------|------|
| `MangoEventPublisher` | 发布事件到事件总线 |
| `ConnectionSessionManager` | 管理 WebSocket 会话 |
| `WebSocketServerManager` | 管理 WebSocket 服务器生命周期 |

## 注意事项

1. **协议类型标识**：`getProtocolType()` 返回值必须与 `PlatformType.code` 一致
2. **线程安全**：消息处理可能在多线程环境中执行，注意并发安全
3. **异常处理**：消息解析失败时应记录日志，避免影响其他消息处理
4. **会话管理**：在 `onConnect` 和 `onDisconnect` 中正确注册/移除会话
