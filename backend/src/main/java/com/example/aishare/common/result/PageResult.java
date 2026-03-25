package com.example.aishare.common.result;

import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code = 200;
    private String message = "success";
    private List<T> data;
    private long total;
    private long current;
    private long size;
    private long pages;
    private long timestamp;

    public static <T> PageResult<T> of(List<T> data, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(size > 0 ? (total + size - 1) / size : 0);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
