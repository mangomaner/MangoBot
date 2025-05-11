# MangoBot

基于Java SpringBoot与OneBot协议实现的QQ机器人框架，实现QQ机器人的极简开发。

## 🌟 功能特性

- **注解驱动**：通过自定义方法注解（如`@AtMessage`, `@TextMessage`, `@ImageMessage`）轻松标识哪些方法应该处理特定类型的消息。
- **参数自动解析**：自定义参数注解，实现对方法参数的自动解析与填充。



## ⚙ 安装与配置

### 	前提条件

- JDK 17
- [LLOneBot插件](https://llonebot.com/zh-CN/guide/getting-started)

安装好后按下图配置：
![image](https://github.com/mangomaner/MangoBot/blob/main/image/LLOneBotconfig.png)

在application.yml中配置好Bot后，@你的Bot，收到回应表示安装成功。



## 🔧 定制指南

1. **定义处理器**：创建带有适当注解的方法来处理不同类型的消息。
2. **注册处理器**：确保你的处理器类被Spring容器管理（例如，使用`@Component`或`@Service`注解）。
3. **发送消息**：提供 GroupMessageService 类，可一键发送不同类型的消息。

简单示例，详见src/main/java/org/mango/mangobot/messageHandler/GroupMessageHandler.java：

```java
@Component
public class MyBot {

    @AtMessage(self = true)
    @TextMessage
    public void handleAtWithText(@Content String content, @SenderId String senderId) {
        System.out.println("收到 @ 自己的文本消息：" + content);
    }

    @PokeMessage
    public void handlePoke(@SenderId String fromUser, @TargetId String targetId) {
        System.out.println(fromUser + " 戳了 " + targetId);
    }
}
```

## 🧩 扩展功能（可选）

MangoBot 提供了一系列高级扩展模块，可用于构建更复杂的机器人系统：

### 🤖 阿里通义千问集成

一键接入阿里云「通义千问」大模型服务，为机器人赋予智能问答能力。只需在配置文件中填入 API Key 即可启用。

### 🔍 RAG 支持（检索增强生成）

通过结合检索与生成技术，提升回复的准确性和上下文相关性。支持以下功能：

- 文本分割：对长文档进行语义分块；
- 向量化：使用阿里多模态向量模型将文本转换为向量；
- MongoDB-atlas 存储：以 MongoDB-atlas 作为向量数据库进行高效检索；
- 智能查询：根据用户问题召回最相关的知识片段；



## 📄 许可证

该项目采用MIT许可证。

(如果您遇到任何问题，欢迎联系 QQ: 2756477287  !!!一定不要嫌麻烦不来啊QAQ，哦捏该！)
