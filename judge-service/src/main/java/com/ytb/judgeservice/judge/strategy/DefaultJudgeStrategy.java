package com.ytb.judgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.dto.question.InputItem;
import com.ytb.model.dto.question.JudgeConfig;
import com.ytb.model.dto.question.OutputItem;
import com.ytb.model.dto.question.TestCase;
import com.ytb.model.entity.Question;
import com.ytb.model.enums.JudgeInfoMessageEnum;


import java.util.List;

/**
 * 默认判题策略
 */
public class DefaultJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<List<InputItem>> inputTestCaseList = judgeContext.getInputTestCaseList();
        List<List<OutputItem>> outputTestResultList = judgeContext.getOutputTestResultList();
        List<TestCase> testCaseList = judgeContext.getTestCaseList();
        Question question = judgeContext.getQuestion();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        JudgeInfo judgeInfoResponse = new JudgeInfo();
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
            if (!testCase.getOutput().equals(outputTestResultList.get(i))){
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
                return judgeInfoResponse;
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
        if (time > needTimeLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getMessage());
        return judgeInfoResponse;

    }
}
