package com.ytb.judgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.ytb.model.codesandbox.ExecuteResultInfo;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.dto.question.InputItem;
import com.ytb.model.dto.question.JudgeConfig;
import com.ytb.model.dto.question.OutputItem;
import com.ytb.model.dto.question.TestCase;
import com.ytb.model.entity.Question;
import com.ytb.model.enums.JudgeInfoMessageEnum;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Java 程序判题策略
 */
public class JavaJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        ExecuteResultInfo executeResultInfo = judgeContext.getExecuteResultInfo();
        List<List<InputItem>> inputTestCaseList = judgeContext.getInputTestCaseList();
        List<List<OutputItem>> outputTestResultList = judgeContext.getOutputTestResultList();
        List<TestCase> testCaseList = judgeContext.getTestCaseList();
        Question question = judgeContext.getQuestion();
        Integer code = executeResultInfo.getCode();
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        if (!Objects.equals(code, JudgeInfoMessageEnum.ACCEPTED.getValue())){
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.getEnumByValue(code).getMessage());
            return judgeInfoResponse;
        }

        Long memory = Optional.ofNullable(executeResultInfo.getMemory()).orElse(0L);
        Long time = Optional.ofNullable(executeResultInfo.getTime()).orElse(0L);
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);

        //先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if (outputTestResultList.size() != inputTestCaseList.size()){
            judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
            return judgeInfoResponse;
        }
        //依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < testCaseList.size(); i++) {
            TestCase testCase = testCaseList.get(i);
            List<OutputItem> standOutputItemList = testCase.getOutput();
            List<OutputItem> answerOutputItemList = outputTestResultList.get(i);
            if (standOutputItemList.size() != answerOutputItemList.size()){
                judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
                return judgeInfoResponse;
            }
            for (int j = 0;j < standOutputItemList.size();j++){
                OutputItem standOutputItem = standOutputItemList.get(j);
                String standParamName = standOutputItem.getParamName();
                String standParamValue = standOutputItem.getParamValue();
                OutputItem answerOutputItem = answerOutputItemList.get(j);
                String answerParamName = answerOutputItem.getParamName();
                String answerParamValue = answerOutputItem.getParamValue().trim();
                if (!standParamName.equals(answerParamName)){
                    judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
                    judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
                    return judgeInfoResponse;
                }
                if (!standParamValue.equals(answerParamValue)){
                    judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
                    judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
                    return judgeInfoResponse;
                }
            }
        }
        //判断题目限制
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needTimeLimit = judgeConfig.getTimeLimit();
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        if (memory > needMemoryLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
            return judgeInfoResponse;
        }
        // 假设Java 程序本身需要额外执行10秒钟
        long JAVA_PROGRAM_TIME_COST = 10000L;
        if ((time- JAVA_PROGRAM_TIME_COST) > needTimeLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
        return judgeInfoResponse;

    }
}
