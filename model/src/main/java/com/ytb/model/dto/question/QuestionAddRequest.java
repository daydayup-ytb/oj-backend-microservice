package com.ytb.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *

 */
@Data
public class QuestionAddRequest implements Serializable {


    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 初始化代码
     */
    private String initialCode;

    /**
     * 判题代码
     */
    private String judgeCode;

    /**
     * 判题用例（json数组）
     */
    private List<TestCase> judgeCase;

    /**
     * 判题配置（json对象）
     */
    private JudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}