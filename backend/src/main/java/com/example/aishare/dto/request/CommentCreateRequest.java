package com.example.aishare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求
 */
@Data
public class CommentCreateRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论长度必须在 1-1000 之间")
    private String content;

    private Long parentId;
}