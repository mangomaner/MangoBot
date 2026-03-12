-- MangoBot 数据库初始化脚本
-- 创建时间: 2026-01-17
-- 最后更新: 2026-03-12（配置系统重构 - 角色与模型分离）

-- ============================================
-- 模型配置相关表
-- ============================================

-- 模型供应商表：存储 API 供应商的公共配置
CREATE TABLE IF NOT EXISTS model_providers (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT NOT NULL UNIQUE,            -- 供应商名称（openai, anthropic, deepseek 等）
    base_url        TEXT NOT NULL,                   -- API 基础地址
    api_key         TEXT NOT NULL,                   -- API 密钥
    timeout         INTEGER DEFAULT 30,              -- 默认超时时间（秒）
    description     TEXT,                            -- 描述
    is_enabled      INTEGER DEFAULT 1,               -- 是否启用：0-禁用, 1-启用
    created_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    updated_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000)
);

-- 模型配置表：存储具体模型配置
CREATE TABLE IF NOT EXISTS model_configs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    model_name      TEXT NOT NULL,                   -- 模型标识（gpt-4o, deepseek-chat 等）
    provider_id     INTEGER NOT NULL,                -- 关联供应商
    temperature     REAL DEFAULT 0.7,                -- 温度参数
    max_tokens      INTEGER,                         -- 最大 Token 数
    top_p           REAL,                            -- Top-P 参数
    timeout         INTEGER,                         -- 超时时间（覆盖供应商默认值）
    description     TEXT,                            -- 描述
    is_enabled      INTEGER DEFAULT 1,               -- 是否启用：0-禁用, 1-启用
    created_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    updated_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    FOREIGN KEY (provider_id) REFERENCES model_providers(id)
);

-- 模型角色表：定义模型角色与模型配置的映射关系
CREATE TABLE IF NOT EXISTS model_roles (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    role_key        TEXT NOT NULL UNIQUE,            -- 角色标识：main, assistant, image, embedding
    role_name       TEXT NOT NULL,                   -- 角色名称：主模型、助手模型等
    model_config_id INTEGER,                         -- 关联的模型配置ID（可为空表示未配置）
    description     TEXT,                            -- 描述
    created_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    updated_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    FOREIGN KEY (model_config_id) REFERENCES model_configs(id)
);

-- 模型供应商索引
CREATE INDEX IF NOT EXISTS idx_model_providers_name ON model_providers (name);
CREATE INDEX IF NOT EXISTS idx_model_providers_enabled ON model_providers (is_enabled);

-- 模型配置索引
CREATE INDEX IF NOT EXISTS idx_model_configs_provider ON model_configs (provider_id);

-- 模型角色索引
CREATE INDEX IF NOT EXISTS idx_model_roles_key ON model_roles (role_key);
CREATE INDEX IF NOT EXISTS idx_model_roles_config ON model_roles (model_config_id);

-- ============================================
-- 系统配置表
-- ============================================

-- 系统配置表：存储框架级配置（白名单、黑名单等）
CREATE TABLE IF NOT EXISTS system_configs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key      TEXT NOT NULL UNIQUE,            -- 配置键
    config_value    TEXT,                            -- 配置值（支持 JSON）
    config_type     TEXT NOT NULL DEFAULT 'STRING',  -- 类型：STRING, INTEGER, BOOLEAN, JSON, SELECT
    description     TEXT,                            -- 描述
    explain         TEXT,                            -- 详细说明
    category        TEXT DEFAULT 'general',          -- 分类
    editable        INTEGER DEFAULT 1,               -- 是否可编辑：0-不可编辑, 1-可编辑
    created_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    updated_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000)
);

-- 系统配置索引
CREATE INDEX IF NOT EXISTS idx_system_configs_key ON system_configs (config_key);
CREATE INDEX IF NOT EXISTS idx_system_configs_category ON system_configs (category);

-- ============================================
-- 插件配置表
-- ============================================

-- 插件配置表：存储插件自定义配置
CREATE TABLE IF NOT EXISTS plugin_configs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    plugin_id       INTEGER NOT NULL,                -- 关联插件
    config_key      TEXT NOT NULL,                   -- 配置键（相对于插件）
    config_value    TEXT,                            -- 配置值
    config_type     TEXT NOT NULL DEFAULT 'STRING',  -- 类型：STRING, INTEGER, BOOLEAN, JSON
    description     TEXT,                            -- 描述
    explain         TEXT,                            -- 详细说明
    editable        INTEGER DEFAULT 1,               -- 是否可编辑：0-不可编辑, 1-可编辑
    created_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    updated_at      INTEGER DEFAULT (strftime('%s', 'now') * 1000),
    UNIQUE(plugin_id, config_key),
    FOREIGN KEY (plugin_id) REFERENCES plugins(id) ON DELETE CASCADE
);

-- 插件配置索引
CREATE INDEX IF NOT EXISTS idx_plugin_configs_plugin ON plugin_configs (plugin_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_plugin_configs_unique ON plugin_configs (plugin_id, config_key);

-- ============================================
-- 初始数据
-- ============================================

-- 模型供应商初始数据
INSERT INTO model_providers (name, base_url, api_key, description) VALUES
    ('openai', 'https://api.openai.com/v1', 'sk-xxx', 'OpenAI 官方接口'),
    ('custom', 'https://api.example.com/v1', 'sk-xxx', '自定义接口');

-- 模型配置初始数据
INSERT INTO model_configs (model_name, provider_id, temperature, description) VALUES
    ('gpt-4o', 1, 0.7, 'GPT-4o 模型'),
    ('gpt-3.5-turbo', 1, 0.7, 'GPT-3.5 Turbo 模型'),
    ('gpt-4-vision-preview', 1, 0.7, 'GPT-4 Vision 模型'),
    ('text-embedding-3-small', 1, 0.7, '文本嵌入模型');

-- 模型角色初始数据
INSERT INTO model_roles (role_key, role_name, model_config_id, description) VALUES
    ('main', '主模型', 1, '用于主要对话任务'),
    ('assistant', '助手模型', 2, '用于简单任务，节省成本'),
    ('image', '图片模型', 3, '用于图片理解任务'),
    ('embedding', '向量模型', 4, '用于文本向量化');

-- 系统配置初始数据
INSERT INTO system_configs (config_key, config_value, config_type, description, explain, category) VALUES
    ('group.whitelist', '{}', 'JSON', '群组白名单', 'key为Bot QQ号，value为群号列表。示例：{"1234567890": [111111, 222222]}', 'BW_list'),
    ('group.blacklist', '{}', 'JSON', '群组黑名单', 'key为Bot QQ号，value为群号列表。示例：{"1234567890": [111111, 222222]}', 'BW_list'),
    ('group.enable_list', '1', 'BOOLEAN', '启用群组黑白名单', '', 'BW_list'),
    ('private.whitelist', '{}', 'JSON', '私聊白名单', 'key为Bot QQ号，value为用户QQ列表。示例：{"1234567890": [111111, 222222]}', 'BW_list'),
    ('private.blacklist', '{}', 'JSON', '私聊黑名单', 'key为Bot QQ号，value为用户QQ列表。示例：{"1234567890": [111111, 222222]}', 'BW_list'),
    ('private.enable_list', '1', 'BOOLEAN', '启用私聊黑白名单', '', 'BW_list');

-- ============================================
-- 消息存储表
-- ============================================

CREATE TABLE IF NOT EXISTS group_messages
(
    id               INTEGER                                        not null
        constraint group_messages_pk
            primary key autoincrement,
    bot_id           INTEGER                                        not null,
    group_id         INTEGER                                        not null,
    message_id       INTEGER,
    sender_id        INTEGER,
    message_segments TEXT,
    message_time     INTEGER default (strftime('%s', 'now') * 1000) not null,
    is_delete        INTEGER DEFAULT 0,
    parse_message    TEXT
);

CREATE INDEX IF NOT EXISTS idx_group_messages_bot_group_time
    ON group_messages (bot_id, group_id, message_time);

CREATE INDEX IF NOT EXISTS group_messages_message_id_index
    ON group_messages (message_id);


CREATE TABLE IF NOT EXISTS private_messages(
    id               INTEGER                                        not null
        constraint private_messages_pk
            primary key autoincrement,
    bot_id           INTEGER                                        not null,
    friend_id        INTEGER                                        not null,
    message_id       INTEGER,
    sender_id        INTEGER,
    message_segments TEXT,
    message_time     INTEGER default (strftime('%s', 'now') * 1000) not null,
    is_delete        INTEGER DEFAULT 0,
    parse_message    TEXT
);

CREATE INDEX IF NOT EXISTS idx_private_messages_bot_group_time
    ON private_messages (bot_id, friend_id, message_time);

CREATE INDEX IF NOT EXISTS private_messages_message_id_index
    ON private_messages (message_id);


CREATE TABLE IF NOT EXISTS bot_files
(
    id             INTEGER not null
        constraint files_pk
            primary key autoincrement,
    file_type      TEXT,
    file_id        TEXT    not null
        constraint files_pk_2
            unique,
    url            TEXT,
    file_path      TEXT,
    sub_type       INTEGER,
    file_size      INTEGER,
    description    TEXT,
    create_time    INTEGER default (strftime('%s', 'now') * 1000)
);


CREATE TABLE IF NOT EXISTS plugins
(
    id                  INTEGER not null
        constraint plugins_pk
            primary key autoincrement,
    plugin_name         TEXT,
    jar_name            TEXT    not null
        constraint plugin_name_pk
            unique,
    author              TEXT    not null,
    version             TEXT,
    description         TEXT,
    enabled             INTEGER default 0,
    enabled_web         INTEGER default 0,
    package_name        TEXT,
    create_time         INTEGER default (strftime('%s', 'now') * 1000)
);


-- Agent 会话表：存储 AgentScope AutoContextMemory 的持久化状态
CREATE TABLE IF NOT EXISTS agent_sessions
(
    id              INTEGER not null
        constraint agent_sessions_pk
            primary key autoincrement,
    session_id      TEXT    not null,
    bot_id          INTEGER not null,
    session_type    TEXT    not null,
    group_id        INTEGER,
    user_id         INTEGER,
    agent_name      TEXT    not null,
    sys_prompt      TEXT,
    memory_state    TEXT,
    message_history TEXT,
    last_active_at  INTEGER default (strftime('%s', 'now') * 1000),
    created_at      INTEGER default (strftime('%s', 'now') * 1000),
    updated_at      INTEGER default (strftime('%s', 'now') * 1000),
    is_active       INTEGER default 1,
    constraint session_id_unique unique (session_id)
);

CREATE INDEX IF NOT EXISTS idx_agent_sessions_session_id
    ON agent_sessions (session_id);

CREATE INDEX IF NOT EXISTS idx_agent_sessions_bot_active
    ON agent_sessions (bot_id, is_active);

CREATE INDEX IF NOT EXISTS idx_agent_sessions_last_active
    ON agent_sessions (last_active_at);
