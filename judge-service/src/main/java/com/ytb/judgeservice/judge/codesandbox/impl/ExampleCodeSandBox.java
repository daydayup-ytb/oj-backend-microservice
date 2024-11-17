package com.ytb.judgeservice.judge.codesandbox.impl;



import com.ytb.judgeservice.judge.codesandbox.CodeSandBox;
import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.codesandbox.ExecuteCodeResponse;

/**
 * 实例代码沙箱（仅为跑通业务流程）
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        List<List<InputItem>> inputList = executeCodeRequest.getInputTestCaseList();
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.set(inputList);
//        executeCodeResponse.setMessage("测试执行成功");
//        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
//        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
//        judgeInfo.setTime(100L);
//        judgeInfo.setMemory(100L);
//        executeCodeResponse.setJudgeInfo(judgeInfo);
//        return executeCodeResponse;
        return null;
    }
}
