package io.github.mangomaner.mangobot.config;

import io.agentscope.core.model.OpenAIChatModel;
import lombok.Getter;

public class AiConfig {

    private AiConfig() {
    }

    @Getter
    private static volatile OpenAIChatModel mainModel = null;
    @Getter
    private static volatile OpenAIChatModel assistantModel = null;
    @Getter
    private static volatile OpenAIChatModel imageModel = null;
    @Getter
    private static volatile OpenAIChatModel embeddingModel = null;

    static void setMainModel(OpenAIChatModel model) {
        mainModel = model;
    }

    static void setAssistantModel(OpenAIChatModel model) {
        assistantModel = model;
    }

    static void setImageModel(OpenAIChatModel model) {
        imageModel = model;
    }

    static void setEmbeddingModel(OpenAIChatModel model) {
        embeddingModel = model;
    }
}
