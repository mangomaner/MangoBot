package io.github.mangomaner.mangobot.module.file.service;

import io.github.mangomaner.mangobot.module.file.model.domain.BotFiles;
import io.github.mangomaner.mangobot.module.file.model.dto.AddFileRequest;
import io.github.mangomaner.mangobot.module.file.model.dto.UpdateFileRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author mangoman
* @description 针对表【files】的数据库操作Service
* @createDate 2026-01-17 23:40:10
*/
public interface BotFilesService extends IService<BotFiles> {

    List<BotFiles> getAllFiles();

    List<BotFiles> getFilesByDescription(String description);

    BotFiles getFileById(Long id);

    BotFiles getFileByFileId(String fileId);

    List<BotFiles> getFilesByType(String fileType);

    Boolean addFile(AddFileRequest request);

    Boolean updateFile(UpdateFileRequest request);

    Boolean deleteFile(Long id);

    Boolean deleteFileByFileId(String fileId);
}
