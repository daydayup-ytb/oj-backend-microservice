package com.ytb.model.vo;

import com.ytb.model.dto.question.InputItem;
import com.ytb.model.dto.question.OutputItem;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRunResultVo {

    /**
     * 结果代码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 执行时间
     */
    private Long executeTime;

    /**
     * 输入
     */
    private List<InputItem> input;

    /**
     * 输出
     */
    private List<OutputItem> output;

    /**
     * 预期输出
     */
    private List<OutputItem> expectOutput;
}
