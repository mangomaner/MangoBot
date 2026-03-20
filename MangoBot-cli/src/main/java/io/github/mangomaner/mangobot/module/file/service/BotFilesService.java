package io.github.mangomaner.mangobot.module.file.service;

import io.github.mangomaner.mangobot.module.file.model.domain.BotFiles;
import io.github.mangomaner.mangobot.module.file.model.dto.AddFileRequest;
import io.github.mangomaner.mangobot.module.file.model.dto.SendFileRequest;
import io.github.mangomaner.mangobot.module.file.model.dto.UpdateFileRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;

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

    /**
     * 保存收到的文件（从消息段）
     * 由消息处理器内部调用，不对外暴露
     * @param segments 消息段列表
     */
    void saveReceivedFiles(List<OneBotMessageSegment> segments);

    /**
     * 保存发送的文件（本地文件）
     * 对外暴露，供 Tool 或其他组件调用
     * @param request 发送文件请求
     * @return 保存的文件记录
     */
    BotFiles saveSentFile(SendFileRequest request);
}
