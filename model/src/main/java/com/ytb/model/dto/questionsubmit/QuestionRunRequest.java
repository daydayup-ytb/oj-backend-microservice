package com.ytb.model.dto.questionsubmit;

import com.ytb.model.dto.question.TestCase;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionRunRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 测试用例
     */
    private List<TestCase> testCaseList;

    private static final long serialVersionUID = 1L;
}
