package io.github.mangomaner.mangobot.module.file.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送文件请求")
public class SendFileRequest {

    @Schema(description = "文件ID（用于唯一标识文件）")
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    @Schema(description = "文件相对路径（相对于应用根目录）")
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    @Schema(description = "文件类型（如 meme, image, video 等）")
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    @Schema(description = "文件子类型（1/11 为表情包）")
    private Integer subType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件描述")
    private String description;
}
