package com.ytb.judgeservice.judge.strategy;

import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.dto.question.InputItem;
import com.ytb.model.entity.Question;

import java.util.List;

public class CoreCodePatternStrategy implements PatternStrategy{
    @Override
    public ExecuteCodeRequest getExecuteCodeRequest(PatternContext patternContext) {

        //docker：测试账号且用户代码以1结尾，方可使用本地代码沙箱(只有这种情况，可以使用本地代码沙箱)
        String language = patternContext.getLanguage();
        String code = patternContext.getCode();
        Question question = patternContext.getQuestion();
        String judgeCode = question.getJudgeCode();
        List<List<InputItem>> inputTestCaseList = patternContext.getInputTestCaseList();
        if ("java".equalsIgnoreCase(language)) {
            //导入util包，scanner在这个包下
            code = "import java.util.*;" + code + judgeCode;
        }
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .language(language)
                .code(code)
                .inputTestCaseList(inputTestCaseList)
                .build();
        return executeCodeRequest;
    }
}
