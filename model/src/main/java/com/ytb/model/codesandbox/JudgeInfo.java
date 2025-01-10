package com.ytb.model.codesandbox;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {


    /**
     * 判题信息
     */
    private String message;


    /**
     * 内存信息
     */
    private Long memory;


    /**
     * 执行时间
     */
    private Long time;
}
