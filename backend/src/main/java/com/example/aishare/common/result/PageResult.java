package com.example.aishare.common.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 分页响应结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageResult<T> of(T data, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
