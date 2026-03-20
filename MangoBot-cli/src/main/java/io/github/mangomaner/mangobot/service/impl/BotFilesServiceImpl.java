package io.github.mangomaner.mangobot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.agent.workspace.ImageParseService;
import io.github.mangomaner.mangobot.model.domain.BotFiles;
import io.github.mangomaner.mangobot.model.dto.AddFileRequest;
import io.github.mangomaner.mangobot.model.dto.SendFileRequest;
import io.github.mangomaner.mangobot.model.dto.UpdateFileRequest;
import io.github.mangomaner.mangobot.model.onebot.segment.*;
import io.github.mangomaner.mangobot.service.BotFilesService;
import io.github.mangomaner.mangobot.mapper.BotFilesMapper;
import io.github.mangomaner.mangobot.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
* @author mangoman
* @description 针对表【files】的数据库操作Service实现
* @createDate 2026-01-17 23:40:10
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class BotFilesServiceImpl extends ServiceImpl<BotFilesMapper, BotFiles>
    implements BotFilesService {

    private final ImageParseService imageParseService;

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
     * 保存收到的文件（从消息段）
     * 由消息处理器内部调用，不对外暴露
     * @param segments 消息段列表
     */
    @Override
    public void saveReceivedFiles(List<MessageSegment> segments) {

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

                int subType = data.getSubType() != null ? data.getSubType() : 0;
                String url = data.getUrl();
                String fileId = data.getFile();

                if (this.getFileByFileId(fileId) != null) {
                    continue;
                }

                String fileType;
                switch (subType) {
                    case 1, 11 -> fileType = "meme";
                    default -> fileType = "image";
                }

                if (isLocalFilePath(fileId)) {
                    saveLocalImageFile(fileId, fileType, subType);
                    continue;
                }

                AddFileRequest request = new AddFileRequest();
                request.setFileId(fileId);
                request.setUrl(url);
                request.setSubType(subType);
                request.setFileSize(data.getFileSize() == null ? null : Integer.parseInt(data.getFileSize()));
                request.setFileType(fileType);

                String targetDir = switch (subType) {
                    case 1, 11 -> "data/meme";
                    default -> "data/image";
                };

                try {
                    String fileName = extractFileName(fileId);
                    String filePath = targetDir + "/" + fileName;
                    Path targetPath = FileUtils.resolvePath(filePath);
                    FileUtils.downloadFile(url, targetPath);
                    request.setFilePath(filePath);
                    log.info("Downloaded image: fileId={}, subType={}, url={}, filePath={}", fileId, subType, url, filePath);
                } catch (Exception e) {
                    log.error("Failed to download image: fileId={}, subType={}, url={}", fileId, subType, url, e);
                }

                String description = null;
                if (url != null && !url.isEmpty()) {
                    if (subType == 1 || subType == 11) {
                        description = imageParseService.parseMeme(url);
                    } else {
                        description = imageParseService.parseImage(url);
                    }

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

    /**
     * 判断是否为本地文件路径
     */
    private boolean isLocalFilePath(String fileId) {
        if (fileId == null || fileId.isEmpty()) {
            return false;
        }
        return fileId.contains(":/") || fileId.contains(":\\") || 
               fileId.startsWith("/") || fileId.startsWith("\\\\");
    }

    /**
     * 保存本地图片文件记录
     */
    private void saveLocalImageFile(String filePath, String fileType, int subType) {
        try {
            String fileName = extractFileName(filePath);
            String relativePath = extractRelativePath(filePath);
            
            if (this.getFileByFileId(fileName) != null) {
                return;
            }

            BotFiles file = new BotFiles();
            file.setFileId(fileName);
            file.setFileType(fileType);
            file.setFilePath(relativePath);
            file.setSubType(subType);
            file.setCreateTime(System.currentTimeMillis());

            Path localPath = FileUtils.resolvePath(relativePath);
            if (Files.exists(localPath)) {
                file.setFileSize((int) Files.size(localPath));
            }

            this.save(file);
            log.info("Saved local image file: fileId={}, filePath={}, fileType={}", fileName, relativePath, fileType);
        } catch (Exception e) {
            log.warn("Failed to save local image file: {}", filePath, e);
        }
    }

    /**
     * 从绝对路径提取相对路径
     */
    private String extractRelativePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        
        Path baseDir = FileUtils.getBaseDirectory();
        Path absolutePath;
        
        try {
            if (Paths.get(filePath).isAbsolute()) {
                absolutePath = Paths.get(filePath);
            } else {
                absolutePath = baseDir.resolve(filePath);
            }
            
            if (absolutePath.startsWith(baseDir)) {
                return baseDir.relativize(absolutePath).toString().replace('\\', '/');
            }
        } catch (Exception e) {
            log.debug("Failed to extract relative path: {}", filePath);
        }
        
        return filePath.replace('\\', '/');
    }

    /**
     * 从 fileId 中提取文件名
     * 处理 fileId 可能是完整路径的情况
     */
    private String extractFileName(String fileId) {
        if (fileId == null || fileId.isEmpty()) {
            return "unknown";
        }
        int lastSeparator = Math.max(
            fileId.lastIndexOf('/'),
            fileId.lastIndexOf('\\')
        );
        if (lastSeparator >= 0 && lastSeparator < fileId.length() - 1) {
            return fileId.substring(lastSeparator + 1);
        }
        return fileId;
    }

    /**
     * 保存发送的文件（本地文件）
     * 对外暴露，供 Tool 或其他组件调用
     * @param request 发送文件请求
     * @return 保存的文件记录
     */
    @Override
    public BotFiles saveSentFile(SendFileRequest request) {
        if (this.getFileByFileId(request.getFileId()) != null) {
            log.warn("File already exists: fileId={}", request.getFileId());
            return this.getFileByFileId(request.getFileId());
        }

        BotFiles file = new BotFiles();
        file.setFileId(request.getFileId());
        file.setFileType(request.getFileType());
        file.setFilePath(request.getFilePath());
        file.setSubType(request.getSubType());
        file.setDescription(request.getDescription());
        file.setCreateTime(System.currentTimeMillis());

        try {
            Path filePath = FileUtils.resolvePath(request.getFilePath());
            if (Files.exists(filePath)) {
                file.setFileSize((int) Files.size(filePath));
            }
        } catch (Exception e) {
            log.warn("Failed to get file size: {}", request.getFilePath());
        }

        if (request.getFileSize() != null) {
            file.setFileSize(request.getFileSize().intValue());
        }

        this.save(file);
        log.info("Saved sent file: fileId={}, filePath={}, fileType={}", 
                request.getFileId(), request.getFilePath(), request.getFileType());
        return file;
    }
}
