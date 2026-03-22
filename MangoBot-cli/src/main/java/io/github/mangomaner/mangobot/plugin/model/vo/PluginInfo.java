package io.github.mangomaner.mangobot.plugin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PluginInfo implements Serializable {
    private Long id;
    private String jarName;
    private boolean loaded;
    private String name;
    private String author;
    private String version;
    private String description;
    private boolean enabled;
    private boolean enableWeb;
    private String webPath;
}
