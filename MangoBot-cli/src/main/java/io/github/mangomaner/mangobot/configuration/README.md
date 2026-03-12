# MangoBot 配置系统说明文档

## 概述

MangoBot 配置系统采用模块化设计，将配置按类型分离存储，提供更清晰的配置管理方式和更好的可维护性。

## 架构设计

### 模块结构

```
configuration/
├── annotation/                    # 配置相关注解
│   └── InjectConfig.java          # 插件配置注入注解
├── controller/                    # 配置管理 API
│   ├── ModelConfigController.java # 模型配置控制器
│   ├── ModelRoleController.java   # 模型角色控制器
│   ├── SystemConfigController.java# 系统配置控制器
│   └── PluginConfigController.java# 插件配置控制器
├── event/                         # 配置事件
│   ├── ConfigurationEvent.java    # 配置事件基类
│   ├── ModelRoleChangedEvent.java # 模型角色变更事件
│   ├── ModelConfigChangedEvent.java
│   ├── SystemConfigChangedEvent.java
│   └── PluginConfigChangedEvent.java
├── mapper/                        # 数据访问层
│   ├── ModelProviderMapper.java
│   ├── ModelConfigMapper.java
│   ├── ModelRoleMapper.java
│   ├── SystemConfigMapper.java
│   └── PluginConfigMapper.java
├── model/                         # 数据模型
│   ├── domain/                    # 实体类
│   │   ├── ModelProvider.java
│   │   ├── ModelConfig.java
│   │   ├── ModelRole.java
│   │   ├── SystemConfig.java
│   │   └── PluginConfigEntity.java
│   ├── dto/                       # 数据传输对象
│   │   ├── model/
│   │   ├── system/
│   │   └── plugin/
│   └── vo/                        # 视图对象
│       ├── ModelProviderVO.java
│       ├── ModelConfigVO.java
│       ├── ModelRoleVO.java
│       ├── SystemConfigVO.java
│       ├── PluginConfigVO.java
│       └── ModelTestResultVO.java
└── service/                       # 服务层
    ├── ModelProvider.java         # 模型提供者接口（对外提供模型实例）
    ├── ModelProviderService.java  # 供应商服务
    ├── ModelConfigService.java    # 模型配置服务
    ├── ModelRoleService.java      # 模型角色服务
    ├── SystemConfigService.java
    ├── PluginConfigService.java
    └── impl/
```

## 数据库表结构

### 1. model_providers（模型供应商表）

存储 API 供应商的公共配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| name | TEXT | 供应商名称（openai, anthropic, deepseek 等） |
| base_url | TEXT | API 基础地址 |
| api_key | TEXT | API 密钥 |
| timeout | INTEGER | 默认超时时间（秒） |
| description | TEXT | 描述 |
| is_enabled | INTEGER | 是否启用 |

### 2. model_configs（模型配置表）

存储具体模型的配置信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| model_name | TEXT | 模型标识（gpt-4o, deepseek-chat 等） |
| provider_id | INTEGER | 关联供应商 |
| temperature | REAL | 温度参数 |
| max_tokens | INTEGER | 最大 Token 数 |
| top_p | REAL | Top-P 参数 |
| timeout | INTEGER | 超时时间 |
| description | TEXT | 描述 |
| is_enabled | INTEGER | 是否启用 |

### 3. model_roles（模型角色表）

定义模型角色与模型配置的映射关系。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| role_key | TEXT | 角色标识（main, assistant, image, embedding） |
| role_name | TEXT | 角色名称（主模型、助手模型等） |
| model_config_id | INTEGER | 关联的模型配置ID |
| description | TEXT | 描述 |

### 4. system_configs（系统配置表）

存储框架级配置（白名单、黑名单等）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| config_key | TEXT | 配置键 |
| config_value | TEXT | 配置值（支持 JSON） |
| config_type | TEXT | 类型（STRING, INTEGER, BOOLEAN, JSON, SELECT） |
| description | TEXT | 描述 |
| explain | TEXT | 详细说明 |
| category | TEXT | 分类 |
| editable | INTEGER | 是否可编辑 |

### 5. plugin_configs（插件配置表）

存储插件自定义配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| plugin_id | INTEGER | 关联插件 |
| config_key | TEXT | 配置键（相对于插件） |
| config_value | TEXT | 配置值 |
| config_type | TEXT | 类型 |
| description | TEXT | 描述 |
| explain | TEXT | 详细说明 |
| editable | INTEGER | 是否可编辑 |

## API 接口

### 模型角色 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/configuration/model/roles | 获取所有角色 |
| GET | /api/configuration/model/roles/{roleKey} | 获取角色详情 |
| PUT | /api/configuration/model/roles/{roleKey} | 更新角色对应的模型 |

### 模型配置 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/configuration/model/providers | 获取所有供应商 |
| GET | /api/configuration/model/providers/{id} | 获取供应商详情 |
| POST | /api/configuration/model/providers | 创建供应商 |
| PUT | /api/configuration/model/providers | 更新供应商 |
| DELETE | /api/configuration/model/providers/{id} | 删除供应商 |
| POST | /api/configuration/model/providers/{id}/test | 测试供应商连接 |
| GET | /api/configuration/model/configs | 获取所有模型配置 |
| GET | /api/configuration/model/configs/{id} | 获取模型配置详情 |
| POST | /api/configuration/model/configs | 创建模型配置 |
| PUT | /api/configuration/model/configs | 更新模型配置 |
| DELETE | /api/configuration/model/configs/{id} | 删除模型配置 |
| POST | /api/configuration/model/configs/{id}/test | 测试模型 |

### 系统配置 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/configuration/system | 获取所有系统配置 |
| GET | /api/configuration/system/category/{category} | 按分类获取配置 |
| GET | /api/configuration/system/{configKey} | 获取指定配置 |
| POST | /api/configuration/system | 创建系统配置 |
| PUT | /api/configuration/system | 更新系统配置 |
| PUT | /api/configuration/system/{configKey} | 更新配置值 |
| DELETE | /api/configuration/system/{id} | 删除系统配置 |

### 插件配置 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/configuration/plugin | 获取所有插件配置 |
| GET | /api/configuration/plugin/{pluginId} | 获取插件的配置列表 |
| GET | /api/configuration/plugin/{pluginId}/{configKey} | 获取单个插件配置 |
| PUT | /api/configuration/plugin | 更新插件配置 |
| PUT | /api/configuration/plugin/{pluginId}/{configKey} | 更新插件配置值 |

## 事件系统

配置变更时通过 `MangoEventPublisher` 广播事件：

### 事件类型

| 事件类 | 说明 |
|--------|------|
| ModelRoleChangedEvent | 模型角色变更事件（角色与模型映射变更） |
| ModelConfigChangedEvent | 模型配置变更事件 |
| SystemConfigChangedEvent | 系统配置变更事件 |
| PluginConfigChangedEvent | 插件配置变更事件 |

### 监听配置变更

```java
@MangoBotEventListener
public boolean onModelRoleChanged(ModelRoleChangedEvent event) {
    String roleKey = event.getRoleKey();
    Long oldConfigId = event.getOldModelConfigId();
    Long newConfigId = event.getNewModelConfigId();
    // 处理角色变更
    return true;
}
```

## AI 模型管理

### 模型角色

| 角色标识 | 角色名称 | 说明 |
|---------|---------|------|
| main | 主模型 | 用于主要对话任务 |
| assistant | 助手模型 | 用于简单任务，节省成本 |
| image | 图片模型 | 用于图片理解任务 |
| embedding | 向量模型 | 用于文本向量化 |

### 获取模型实例

通过 `MangoModelApi` 静态工具类获取模型实例：

```java
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.agentscope.core.model.OpenAIChatModel;

public void example() {
    // 获取主模型
    OpenAIChatModel mainModel = MangoModelApi.getMainModel();
    
    // 获取助手模型
    OpenAIChatModel assistantModel = MangoModelApi.getAssistantModel();
    
    // 获取图片模型
    OpenAIChatModel imageModel = MangoModelApi.getImageModel();
    
    // 获取向量模型
    OpenAIChatModel embeddingModel = MangoModelApi.getEmbeddingModel();
    
    // 通过角色标识获取模型
    OpenAIChatModel model = MangoModelApi.getModel("main");
    
    // 获取模型配置信息
    ModelConfigVO config = MangoModelApi.getModelConfig("main");
    
    // 获取所有角色
    List<ModelRoleVO> roles = MangoModelApi.getAllRoles();
    
    // 更新角色对应的模型
    MangoModelApi.updateRoleModel("main", 1L);
    
    // 刷新模型（重新加载）
    MangoModelApi.refreshModel("main");
}
```

### MangoModelApi 方法列表

| 方法 | 说明 |
|------|------|
| `getMainModel()` | 获取主模型实例 |
| `getAssistantModel()` | 获取助手模型实例 |
| `getImageModel()` | 获取图片模型实例 |
| `getEmbeddingModel()` | 获取向量模型实例 |
| `getModel(roleKey)` | 获取指定角色的模型实例 |
| `getModelConfig(roleKey)` | 获取指定角色的模型配置详情 |
| `refreshModel(roleKey)` | 刷新指定角色的模型 |
| `getAllRoles()` | 获取所有角色配置 |
| `getRole(roleKey)` | 获取指定角色的配置 |
| `updateRoleModel(roleKey, modelConfigId)` | 更新角色对应的模型 |

### 模型角色变更

当角色对应的模型变更时，`ModelProviderImpl` 会自动监听 `ModelRoleChangedEvent` 并重新加载模型实例。

## 插件配置注入

### 使用 @InjectConfig 注解

在插件类中标记需要注入的配置字段：

```java
@PluginDescribe(name = "MyPlugin", author = "developer", version = "1.0.0")
public class MyPlugin implements Plugin {

    @InjectConfig(key = "timeout", defaultValue = "30", description = "超时时间（秒）")
    private Integer timeout;

    @InjectConfig(key = "greeting", defaultValue = "你好", description = "问候语")
    private String greeting;

    @InjectConfig(key = "enabled", defaultValue = "true", description = "是否启用")
    private Boolean enabled;

    @Override
    public void onEnable() {
        // 配置字段已自动注入
        System.out.println("超时时间: " + timeout);
        System.out.println("问候语: " + greeting);
    }
}
```

### 支持的类型

- STRING
- INTEGER
- LONG
- DOUBLE
- BOOLEAN

### 兼容旧注解

仍然支持 `@PluginConfig` 和 `@PluginConfigs` 注解：

```java
@PluginConfig(key = "plugin.example.hello", value = "你好", description = "测试配置")
@PluginDescribe(name = "ExamplePlugin", author = "mangomaner", version = "1.0.0")
public class ExamplePlugin implements Plugin {
    // ...
}
```

## 迁移指南

### 从旧配置系统迁移

1. **删除旧数据库**：删除 `data/mangobot.db` 文件
2. **重启应用**：应用启动时会自动创建新的表结构
3. **重新配置**：通过 API 或前端界面重新配置模型和系统设置

### 配置迁移对照

| 旧配置键 | 新配置位置 |
|---------|-----------|
| main.model.main_model | model_roles 表（role_key='main'） |
| main.model.assistant_model | model_roles 表（role_key='assistant'） |
| main.model.image_model | model_roles 表（role_key='image'） |
| main.model.embedding_model | model_roles 表（role_key='embedding'） |
| main.QQ.group.whitelist | system_configs 表（config_key='group.whitelist'） |
| main.QQ.group.blacklist | system_configs 表（config_key='group.blacklist'） |
| plugin.* | plugin_configs 表 |

## 最佳实践

1. **模型配置**：先创建供应商，再创建模型配置，最后通过角色分配模型
2. **角色管理**：使用角色来灵活切换不同用途的模型，而非硬编码别名
3. **配置命名**：使用点分隔符命名配置键，如 `feature.timeout`
4. **类型选择**：根据配置值选择合适的类型（STRING, INTEGER, BOOLEAN, JSON）
5. **事件监听**：需要响应配置变更时，监听对应的事件类型
6. **插件配置**：使用 `@InjectConfig` 注解实现类型安全的配置注入
