package com.ytb.judgeservice.judge.strategy;

import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.dto.question.InputItem;

import java.util.List;

public class AcmPatternStrategy implements PatternStrategy{
    @Override
    public ExecuteCodeRequest getExecuteCodeRequest(PatternContext patternContext) {

        String code = patternContext.getCode();
        String language = patternContext.getLanguage();
        List<List<InputItem>> inputTestCaseList = patternContext.getInputTestCaseList();

        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputTestCaseList(inputTestCaseList)
                .build();
        return executeCodeRequest;
    }
}
