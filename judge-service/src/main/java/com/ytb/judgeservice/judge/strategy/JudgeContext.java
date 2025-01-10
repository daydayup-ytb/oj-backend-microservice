package com.ytb.judgeservice.judge.strategy;

import com.ytb.model.codesandbox.ExecuteResultInfo;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.dto.question.InputItem;
import com.ytb.model.dto.question.OutputItem;
import com.ytb.model.dto.question.TestCase;
import com.ytb.model.entity.Question;
import com.ytb.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private ExecuteResultInfo executeResultInfo;

    private List<List<InputItem>> inputTestCaseList;

    private List<List<OutputItem>> outputTestResultList;

    private List<TestCase> testCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;
}
