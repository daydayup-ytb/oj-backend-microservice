package com.ytb.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRunVo {

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
    private List<QuestionRunResultVo> questionRunResultVoList;


}
