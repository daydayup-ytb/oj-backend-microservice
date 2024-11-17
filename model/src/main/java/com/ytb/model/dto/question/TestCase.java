package com.ytb.model.dto.question;

import lombok.Data;

import java.util.List;


/**
 * 测试用例
 */
@Data
public class TestCase {

    private Long id;

    /**
     * 输入用例
     */
    private List<InputItem> input;

    /**
     * 输出用例
     */
    private List<OutputItem> output;
}
