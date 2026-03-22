package io.github.mangomaner.mangobot.plugin.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.mangomaner.mangobot.annotation.PluginDescribe;
import io.github.mangomaner.mangobot.annotation.web.MangoBotRequestMapping;
import io.github.mangomaner.mangobot.module.configuration.core.PluginConfigRegistry;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.infra.MangoEventPublisher;
import io.github.mangomaner.mangobot.plugin.model.domain.Plugins;
import io.github.mangomaner.mangobot.plugin.model.vo.PluginInfo;
import io.github.mangomaner.mangobot.annotation.InjectConfig;
import io.github.mangomaner.mangobot.plugin.Plugin;
import io.github.mangomaner.mangobot.plugin.core.register.PluginRegistrar;
import io.github.mangomaner.mangobot.plugin.core.unregister.PluginUnloader;
import io.github.mangomaner.mangobot.plugin.service.PluginsService;
import io.github.mangomaner.mangobot.utils.FileUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
@Slf4j
public class PluginManager {

    @Resource
    private MangoEventPublisher eventPublisher;

    @Resource
    private ConfigurableApplicationContext applicationContext;

    @Resource
    private PluginRegistrar pluginRegistrar;

    @Resource
    private PluginUnloader pluginUnloader;

    @Resource
    private PluginsService pluginsService;

    @Resource
    private PluginConfigService pluginConfigService;

    @Resource
    private PluginConfigRegistry pluginConfigRegistry;

    private final Map<String, PluginRuntimeWrapper> pluginRegistry = new ConcurrentHashMap<>();
    private String pluginDir = "plugins";

    public void init() {
        Path baseDir = FileUtils.getBaseDirectory();
        Path currentPlugins = baseDir.resolve("plugins");
        Path parentPlugins = baseDir.resolve("../plugins");

        File current = currentPlugins.toFile();
        File parent = parentPlugins.toFile();

        if (!current.exists() && parent.exists() && parent.isDirectory()) {
            this.pluginDir = parent.getAbsolutePath();
            log.info("检测到上一级目录存在 plugins 文件夹，将使用: {}", parent.getAbsolutePath());
        } else {
            this.pluginDir = current.getAbsolutePath();
            log.info("使用默认插件目录: {}", current.getAbsolutePath());
        }

        pluginRegistrar.registerWebComponents();
        syncPlugins();
    }

    public File getPluginDirectory() {
        return new File(pluginDir);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncPlugins() {
        log.info("开始同步插件...");
        File dir = getPluginDirectory();
        if (!dir.exists()) {
            FileUtils.createDirectory(dir.toPath());
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        List<File> fileList = files != null ? Arrays.asList(files) : Collections.emptyList();
        Set<String> fileNames = new HashSet<>();
        for (File f : fileList) {
            fileNames.add(f.getName());
        }

        List<Plugins> dbPlugins = pluginsService.list();

        for (Plugins p : dbPlugins) {
            if (!fileNames.contains(p.getJarName())) {
                log.info("发现残留插件记录，正在清理: {}", p.getJarName());
                uninstallPluginData(p);
            }
        }

        for (File file : fileList) {
            boolean exists = dbPlugins.stream().anyMatch(p -> p.getJarName().equals(file.getName()));
            if (!exists) {
                log.info("发现新插件文件，正在注册: {}", file.getName());
                scanAndRegister(file);
            }
        }

        List<Plugins> enabledPlugins = pluginsService.list(new LambdaQueryWrapper<Plugins>().eq(Plugins::getEnabled, 1));
        for (Plugins p : enabledPlugins) {
            File file = new File(dir, p.getJarName());
            if (file.exists()) {
                loadPlugin(file);
            }
        }

        eventPublisher.printAllListeners();
        log.info("插件同步完成");
    }

    public void handleNewFile(File file) {
        Long pluginId = scanAndRegister(file);
        if (pluginId == null) return;

        Plugins p = pluginsService.getById(pluginId);
        if (p != null && p.getEnabled() == 1) {
            loadPlugin(file);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Long scanAndRegister(File jarFile) {
        String jarName = jarFile.getName();
        PluginClassLoader loader = null;
        try {
            loader = PluginClassLoader.create(jarFile, getClass().getClassLoader(), null);
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) continue;

                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    String[] parts = className.split("\\.");
                    if (!(parts.length > 3 && parts[0].equals("io") && parts[1].equals("github") && parts[3].equals("mangobot"))) {
                        continue;
                    }

                    try {
                        Class<?> clazz = loader.loadClass(className);
                        if (Plugin.class.isAssignableFrom(clazz) && !clazz.isInterface() && clazz.isAnnotationPresent(PluginDescribe.class)) {
                            PluginDescribe describe = clazz.getAnnotation(PluginDescribe.class);

                            Plugins plugin = getOrCreatePlugin(jarName);
                            plugin.setPluginName(describe.name());
                            plugin.setAuthor(describe.author());
                            plugin.setVersion(describe.version());
                            plugin.setDescription(describe.description());
                            plugin.setPackageName(className);
                            plugin.setEnabledWeb(describe.enableWeb() ? 1 : 0);
                            if (plugin.getId() == null) {
                                plugin.setEnabled(0);
                            }
                            pluginsService.saveOrUpdate(plugin);

                            pluginConfigRegistry.scanAndRegister(clazz, plugin.getId());

                            log.info("插件注册成功: {}", jarName);
                            return plugin.getId();
                        }
                    } catch (Throwable t) {
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描插件失败: {}", jarName, e);
        } finally {
            if (loader != null) {
                try { loader.close(); } catch (IOException e) {}
            }
        }
        return null;
    }

    private Plugins getOrCreatePlugin(String jarName) {
        LambdaQueryWrapper<Plugins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plugins::getJarName, jarName);
        Plugins p = pluginsService.getOne(wrapper);
        if (p == null) {
            p = new Plugins();
            p.setJarName(jarName);
        }
        return p;
    }

    public void loadPlugin(File jarFile) {
        String pluginId = jarFile.getName();
        if (pluginRegistry.containsKey(pluginId)) {
            return;
        }

        log.info("正在加载插件: {}", pluginId);
        PluginClassLoader loader = null;
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Plugins p = pluginsService.getOne(new LambdaQueryWrapper<Plugins>().eq(Plugins::getJarName, pluginId));
            if (p == null) {
                log.error("插件未注册，无法加载: {}", pluginId);
                return;
            }

            loader = PluginClassLoader.create(jarFile, getClass().getClassLoader(), p.getId());
            Thread.currentThread().setContextClassLoader(loader);
            PluginRuntimeWrapper wrapper = new PluginRuntimeWrapper(pluginId, loader);

            Class<?> clazz = loader.loadClass(p.getPackageName());

            if (clazz.isAnnotationPresent(PluginDescribe.class)) {
                wrapper.setDescribe(clazz.getAnnotation(PluginDescribe.class));
            }

            boolean isRequestMapping = clazz.isAnnotationPresent(MangoBotRequestMapping.class);
            Object instance = null;

            if (isRequestMapping) {
                pluginRegistrar.registerController(clazz, wrapper);
                String beanName = clazz.getName();
                instance = applicationContext.getBean(beanName);
            }

            instance = instance == null ? clazz.getDeclaredConstructor().newInstance() : instance;
            Plugin plugin = (Plugin) instance;
            wrapper.setPluginInstance(plugin);

            pluginRegistrar.registerEventListeners(clazz, instance, wrapper);

            injectPluginConfigs(clazz, instance, p.getId());

            plugin.onEnable();
            pluginRegistry.put(pluginId, wrapper);
            log.info("插件加载成功: {}", pluginId);

        } catch (Exception e) {
            log.error("加载插件失败: {}", pluginId, e);
            if (loader != null) {
                try { loader.close(); } catch (IOException ex) {}
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private void injectPluginConfigs(Class<?> clazz, Object instance, Long pluginId) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectConfig.class)) {
                InjectConfig ic =
                    field.getAnnotation(InjectConfig.class);
                String configValue = pluginConfigService.getConfigValueOrDefault(pluginId, ic.key(), ic.defaultValue());

                try {
                    field.setAccessible(true);
                    Object convertedValue = convertValue(configValue, field.getType());
                    field.set(instance, convertedValue);
                    log.debug("注入配置: {} = {}", ic.key(), configValue);
                } catch (Exception e) {
                    log.warn("注入配置失败: {} -> {}", ic.key(), field.getName(), e);
                }
            }
        }
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) {
            return getDefaultValue(targetType);
        }

        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    private Object getDefaultValue(Class<?> targetType) {
        if (targetType == boolean.class) return false;
        if (targetType == int.class) return 0;
        if (targetType == long.class) return 0L;
        if (targetType == double.class) return 0.0;
        return null;
    }

    public void unloadPlugin(String pluginId) {
        PluginRuntimeWrapper wrapper = pluginRegistry.remove(pluginId);
        if (wrapper != null) {
            log.info("正在卸载插件: {}", pluginId);
            pluginUnloader.unload(wrapper);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void uninstallPlugin(String jarName, Long pluginId) {
        unloadPlugin(jarName);

        File file = new File(getPluginDirectory(), jarName);
        if (file.exists()) {
            file.delete();
        }

        Plugins p = pluginsService.getById(pluginId);
        if (p != null) {
            uninstallPluginData(p);
        }
    }

    private void uninstallPluginData(Plugins p) {
        if (p.getEnabledWeb() != null && p.getEnabledWeb() == 1) {
            deleteWebResources(p.getJarName());
        }
        pluginConfigService.deleteByPluginId(p.getId());
        pluginsService.removeById(p.getId());
    }

    private void deleteWebResources(String jarName) {
        String folderName = jarName.replace(".jar", "");
        Path baseDir = FileUtils.getBaseDirectory();
        Path webDir = baseDir.resolve("web").resolve(folderName);
        Path parentWebDir = baseDir.resolve("../web").resolve(folderName);

        File webDirFile = webDir.toFile();
        File parentWebDirFile = parentWebDir.toFile();

        if (webDirFile.exists()) {
            deleteDirectory(webDirFile);
        } else if (parentWebDirFile.exists()) {
            deleteDirectory(parentWebDirFile);
        }
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) deleteDirectory(f);
            }
        }
        file.delete();
    }

    public List<PluginInfo> getAllPluginsInfo() {
        List<Plugins> allPlugins = pluginsService.list();
        List<PluginInfo> result = new ArrayList<>();

        for (Plugins p : allPlugins) {
            boolean loaded = pluginRegistry.containsKey(p.getJarName());
            String jarNameWithoutExt = p.getJarName().replace(".jar", "");

            result.add(PluginInfo.builder()
                    .id(p.getId())
                    .jarName(p.getJarName())
                    .loaded(loaded)
                    .name(p.getPluginName())
                    .author(p.getAuthor())
                    .version(p.getVersion())
                    .description(p.getDescription())
                    .enabled(p.getEnabled() == 1)
                    .enableWeb(p.getEnabledWeb() == 1)
                    .webPath("static/" + jarNameWithoutExt)
                    .build());
        }
        return result;
    }
}
