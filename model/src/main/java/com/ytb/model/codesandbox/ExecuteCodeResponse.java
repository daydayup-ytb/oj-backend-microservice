package com.ytb.model.codesandbox;

import com.ytb.model.dto.question.OutputItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {

    private Integer code;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;


    /**
     * 错误信息
     */
    private String errorInfo;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    private List<OutputItem> outputTestResult;

    private List<List<OutputItem>> outputTestResultList;
}
