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

    /**
     * 代码沙箱执行结果信息
     */
    private ExecuteResultInfo executeResultInfo;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    private List<OutputItem> outputTestResult;

    private List<List<OutputItem>> outputTestResultList;
}
