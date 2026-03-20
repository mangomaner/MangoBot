package io.github.mangomaner.mangobot.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.module.file.model.domain.BotFiles;
import io.github.mangomaner.mangobot.system.mapper.BotFilesMapper;
import io.github.mangomaner.mangobot.module.file.model.dto.AddFileRequest;
import io.github.mangomaner.mangobot.module.file.model.dto.UpdateFileRequest;
import io.github.mangomaner.mangobot.module.file.service.BotFilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
