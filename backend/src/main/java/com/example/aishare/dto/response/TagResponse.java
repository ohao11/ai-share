package com.example.aishare.dto.response;

import lombok.Data;

/**
 * 标签信息响应
 */
@Data
public class TagResponse {

    private Long id;

    private String name;

    private String slug;
}