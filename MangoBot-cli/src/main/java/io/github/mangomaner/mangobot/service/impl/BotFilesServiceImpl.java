package io.github.mangomaner.mangobot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.*;
import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.github.mangomaner.mangobot.api.ModelRole;
import io.github.mangomaner.mangobot.model.domain.BotFiles;
import io.github.mangomaner.mangobot.model.dto.AddFileRequest;
import io.github.mangomaner.mangobot.model.dto.UpdateFileRequest;
import io.github.mangomaner.mangobot.model.onebot.segment.*;
import io.github.mangomaner.mangobot.service.BotFilesService;
import io.github.mangomaner.mangobot.mapper.BotFilesMapper;
import io.github.mangomaner.mangobot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
* @author mangoman
* @description 针对表【files】的数据库操作Service实现
* @createDate 2026-01-17 23:40:10
*/
@Service
@Slf4j
public class BotFilesServiceImpl extends ServiceImpl<BotFilesMapper, BotFiles>
    implements BotFilesService {

    private final String memeParsePrompt = """
            # Role
            你是一位擅长捕捉视觉情绪的表情包分析师。
                        
            # Task
            请分析上传图片，将**关键的视觉特征**与**核心情感**直接挂钩，解释"画面表现了什么"以及"这代表了什么情绪"。
                        
            # Output Format
            请仅输出**一段连贯的文字**（约 50-80 字），结构如下：
            先完整输出屏幕中展示的文字（不存在则跳过该部分），然后简要描述图中**最传神的视觉细节**（如眼神、表情、肢体动作），紧接着用精准的形容词定义其表达的**核心情感基调**（需包含情绪的细微层次）。
            """;

    private final String imageParsePrompt = """
            # Role
            你是一位严谨的"视觉事实记录员"。你的任务是客观、准确地还原图片中的可见内容，不进行任何主观推测、情感渲染或故事化叙述。
                        
            # Guidelines
            1. **绝对客观**：只描述画面中实际存在的物体、人物动作、场景布局和数据事实。禁止使用"看起来"、"似乎"、"可能"、"感觉"等推测性词汇。
            2. **去位置化**：提取文字时，只需列出内容本身，无需说明文字在图中的具体位置（如"左上角"、"底部"）。
            3. **结构化优先**：输出应条理清晰，直接呈现事实清单和文字内容。
            4. **完整性**：尽可能覆盖图中所有可见的关键元素和文本信息。
                        
            # Task
            请对上传的图片执行以下两步操作：
                        
            ## 1. 客观内容陈述 (Objective Description)
            - 以清单或简练段落的形式，罗列图中的核心视觉元素。
            - **如果是场景/人物**：描述主体是谁、穿着什么、正在做什么动作、周围有什么具体物体。
            - **如果是图表/数据**：直接陈述图表类型、坐标轴含义、关键数据点及数值对比。
            - **如果是文档/界面**：描述文档的结构板块、界面的主要功能区域。
            - *注意：严禁添加背景故事、情绪分析或对未发生事情的预测。*
                        
            ## 2. 纯文本提取 (Pure Text Extraction)
            - 按阅读逻辑提取图中所有可见文字。
            - 保持原文的层级结构（如：标题、副标题、正文、列表项）。
            - 仅输出文字内容，不要标注位置或解释其作用。
            - 若遇到模糊不清的字，用 `[?]` 标记。
                        
            # Output Format
            请严格遵循以下格式输出：
                        
            ---
            ### 📷 客观视觉事实
            - [事实点 1：例如：画面中央有一名身穿红色制服的男子，手持麦克风正在讲话。]
            - [事实点 2：例如：背景是一个带有蓝色横幅的会议室，横幅上印有白色标志。]
            - [事实点 3：例如：左侧桌面上放置着一台打开的笔记本电脑和一个水杯。]
            *(若是图表/数据图，请直接陈述数据事实，如：2023 年销售额为 500 万，同比增长 20%。)*
                        
            ### 📄 图中文字内容
            **[标题/主标语]**
            [提取的标题文字]
                        
            **[正文/详细信息]**
            [提取的正文段落或列表内容]
            [提取的其他标签、按钮文字或数据标注]
                        
            *(注：若文字较少，可直接合并列出；若较多，请按视觉层级分段；若很多，如超过150字，请在保留原义的前提下进行压缩)*
            ---
            """;

    private String parseImage(String systemPrompt, String url) {
        try {
            OpenAIChatModel model = MangoModelApi.getModel(ModelRole.IMAGE);
            if (model == null) {
                log.warn("Image model not available, skipping image parsing for url: {}", url);
                return null;
            }
            
            ImageBlock imageBlock = ImageBlock.builder()
                    .source(URLSource.builder()
                            .url(url)
                            .build())
                    .build();
            Msg singleImageMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .content(List.of(
                            TextBlock.builder().text("请你进行分析").build(),
                            imageBlock
                    ))
                    .build();
            ReActAgent agent = ReActAgent.builder()
                    .name("VisionAssistant-" + url)
                    .sysPrompt(systemPrompt)
                    .model(model)
                    .build();
            Msg response = agent.call(singleImageMsg).block();
            return "<image>" + response.getTextContent() + "</image>";
        } catch (Exception e) {
            log.error("Failed to parse image: url={}", url, e);
            return null;
        }
    }

    @Override
    public List<BotFiles> getAllFiles() {
        return this.list();
    }

    @Override
    public List<BotFiles> getFilesByDescription(String description) {
        LambdaQueryWrapper<BotFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(BotFiles::getDescription, description);
        return this.list(wrapper);
    }

    @Override
    public BotFiles getFileById(Long id) {
        return this.getById(id);
    }

    @Override
    public BotFiles getFileByFileId(String fileId) {
        LambdaQueryWrapper<BotFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotFiles::getFileId, fileId);
        return this.getOne(wrapper);
    }

    @Override
    public List<BotFiles> getFilesByType(String fileType) {
        LambdaQueryWrapper<BotFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotFiles::getFileType, fileType);
        return this.list(wrapper);
    }

    @Override
    public Boolean addFile(AddFileRequest request) {
        if (this.getFileByFileId(request.getFileId()) != null) {
            return false;
        }
        BotFiles files = new BotFiles();
        files.setFileType(request.getFileType());
        files.setFileId(request.getFileId());
        files.setUrl(request.getUrl());
        files.setFilePath(request.getFilePath());
        files.setSubType(request.getSubType());
        files.setFileSize(request.getFileSize());
        files.setDescription(request.getDescription());
        files.setCreateTime(System.currentTimeMillis());
        return this.save(files);
    }

    @Override
    public Boolean updateFile(UpdateFileRequest request) {
        BotFiles files = this.getById(request.getId());
        if (files == null) {
            return false;
        }
        if (request.getFileType() != null) {
            files.setFileType(request.getFileType());
        }
        if (request.getUrl() != null) {
            files.setUrl(request.getUrl());
        }
        if (request.getFilePath() != null) {
            files.setFilePath(request.getFilePath());
        }
        if (request.getSubType() != null) {
            files.setSubType(request.getSubType());
        }
        if (request.getFileSize() != null) {
            files.setFileSize(request.getFileSize());
        }
        if (request.getDescription() != null) {
            files.setDescription(request.getDescription());
        }
        return this.updateById(files);
    }

    @Override
    public Boolean deleteFile(Long id) {
        return this.removeById(id);
    }

    @Override
    public Boolean deleteFileByFileId(String fileId) {
        LambdaQueryWrapper<BotFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotFiles::getFileId, fileId);
        return this.remove(wrapper);
    }

    /**
     * 保存文件到数据库
     * @param segments
     * @return
     */
    @Override
    public void saveFileBySegments(List<MessageSegment> segments) {

        for (MessageSegment segment : segments) {
            if (segment instanceof FileSegment) {
                AddFileRequest request = new AddFileRequest();
                FileSegment.FileData data = ((FileSegment) segment).getData();
                request.setFileId(data.getFileId());
                request.setFileType("file");
                request.setUrl(data.getUrl());
                request.setFileSize(Integer.parseInt(data.getFileSize()));
                request.setDescription(data.getFile());
                this.addFile(request);
            } else if (segment instanceof ImageSegment) {
                ImageSegment.ImageData data = ((ImageSegment) segment).getData();

                int subType = data.getSubType();
                String url = data.getUrl();
                String fileId = data.getFile();
                
                if (this.getFileByFileId(fileId) != null) {
                    continue;
                }
                
                AddFileRequest request = new AddFileRequest();
                request.setFileId(fileId);
                request.setUrl(url);
                request.setSubType(subType);
                request.setFileSize(Integer.parseInt(data.getFileSize()));

                String fileType;
                String targetDir;
                switch (subType) {
                    case 1, 11 -> {
                        fileType = "meme";
                        targetDir = "data/meme";
                    }
                    default -> {
                        fileType = "image";
                        targetDir = "data/image";
                    }
                }

                request.setFileType(fileType);

                try {
                    String filePath = targetDir + "/" + fileId;
                    Path targetPath = FileUtils.resolvePath(filePath);
                    FileUtils.downloadFile(url, targetPath);
                    request.setFilePath(filePath);
                    log.info("Downloaded image: fileId={}, subType={}, url={}, filePath={}", fileId, subType, url, filePath);
                } catch (Exception e) {
                    log.error("Failed to download image: fileId={}, subType={}, url={}", fileId, subType, url, e);
                }

                String description = null;
                if (url != null && !url.isEmpty()) {
                    String prompt = (subType == 1 || subType == 11) ? memeParsePrompt : imageParsePrompt;
                    description = parseImage(prompt, url);
                    if (description != null) {
                        log.info("Parsed image description: fileId={}, description={}", fileId, description);
                    }
                }
                request.setDescription(description);

                this.addFile(request);
            } else if (segment instanceof VideoSegment) {
                VideoSegment.VideoData data = ((VideoSegment) segment).getData();
                AddFileRequest request = new AddFileRequest();
                request.setFileId(data.getFile());
                request.setFileType("video");
                request.setUrl(data.getUrl());
                this.addFile(request);
            } else if (segment instanceof RecordSegment) {
                RecordSegment.RecordData data = ((RecordSegment) segment).getData();
                AddFileRequest request = new AddFileRequest();
                request.setFileId(data.getFile());
                request.setFileType("record");
                request.setUrl(data.getUrl());
                this.addFile(request);
            }
        }
    }
}




