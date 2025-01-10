package com.ytb.judgeservice.judge;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;

import com.ytb.common.common.ErrorCode;
import com.ytb.common.exception.BusinessException;
import com.ytb.judgeservice.judge.codesandbox.CodeSandBox;
import com.ytb.judgeservice.judge.codesandbox.CodeSandBoxFactory;
import com.ytb.judgeservice.judge.codesandbox.CodeSandBoxProxy;
import com.ytb.judgeservice.judge.strategy.JudgeContext;
import com.ytb.judgeservice.judge.strategy.PatternContext;
import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.codesandbox.ExecuteCodeResponse;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.dto.question.InputItem;
import com.ytb.model.dto.question.OutputItem;
import com.ytb.model.dto.question.TestCase;
import com.ytb.model.dto.questionsubmit.QuestionRunRequest;
import com.ytb.model.entity.Question;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.model.enums.JudgeInfoMessageEnum;
import com.ytb.model.enums.JudgePatternEnum;
import com.ytb.model.enums.QuestionSubmitLanguageEnum;
import com.ytb.model.enums.QuestionSubmitStatusEnum;
import com.ytb.model.vo.QuestionRunResultVo;
import com.ytb.model.vo.QuestionRunVo;
import com.ytb.serviceclient.QuestionFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService{

    @Resource
    private QuestionFeignClient questionFeignClient;


    @Resource
    private JudgeManager judgeManager;

    @Resource
    private PatternManager patternManager;

    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1.传入题目的提交id，获取相对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        //如果题目状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITTING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目正在判题中");
        }
        //更改判题（题目提交）的状态为“判题中”,防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误");
        }
        // 调用沙箱 获取执行结果
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        codeSandBox = new CodeSandBoxProxy(codeSandBox);
        //获取编程语言
        String language = questionSubmit.getLanguage();
        //获取执行代码
        String code = questionSubmit.getCode();
        //获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<TestCase> testCaseList = JSONUtil.toList(judgeCaseStr, TestCase.class);
        List<List<InputItem>> inputTestCaseList = testCaseList.stream().map(TestCase::getInput).collect(Collectors.toList());
        //根据不用的判题模式获取执行代码请求
        PatternContext patternContext = new PatternContext();
        //查询题目的判题模式
        Integer pattern = question.getPattern();
        JudgePatternEnum judgePatternEnum = JudgePatternEnum.getEnumByValue(pattern);
        if (judgePatternEnum == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"判题模式不存在");
        }
        //根据不同的模式（args core acm）设置模式上下文信息
        patternContext.setPattern(judgePatternEnum.getText());
        patternContext.setCode(code);
        patternContext.setLanguage(language);
        patternContext.setInputTestCaseList(inputTestCaseList);
        patternContext.setQuestion(question);
        //获取代码沙箱执行请求
        ExecuteCodeRequest executeCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
        //获取代码沙箱执行结果
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        List<List<OutputItem>> outputTestResultList = executeCodeResponse.getOutputTestResultList();
        //根据沙箱的执行结果，设置判题上下文信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setExecuteResultInfo(executeCodeResponse.getExecuteResultInfo());
        judgeContext.setInputTestCaseList(inputTestCaseList);
        judgeContext.setOutputTestResultList(outputTestResultList);
        judgeContext.setTestCaseList(testCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        //获取判题结果
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        //修改数据库中的判题结果
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误");
        }
        //返回判题结果信息
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
        return questionSubmitResult;
    }

    @Override
    public QuestionRunVo doRun(QuestionRunRequest questionRunRequest) {
        //1.传入题目的提交id，获取相对应的题目、提交信息（包含代码、编程语言等）
        Long questionId = questionRunRequest.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        String language = questionRunRequest.getLanguage();
        //校验编程语言是否合法
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编程语言错误");
        }
        String code = questionRunRequest.getCode();
        //校验执行代码是否合法
        if (StringUtils.isBlank(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"执行代码为空");
        }
        List<TestCase> testCaseList = questionRunRequest.getTestCaseList();
        //校验测试用例是否合法
        if (testCaseList.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"测试用例为空");
        }
        //调用沙箱 获取执行结果
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        codeSandBox = new CodeSandBoxProxy(codeSandBox);
        List<List<InputItem>> inputTestCaseList = testCaseList.stream().map(TestCase::getInput).collect(Collectors.toList());
        List<QuestionRunResultVo> questionRunResultVoList = new ArrayList<>();
        QuestionRunVo questionRunVo = new QuestionRunVo();
        //题目运行是否通过
        boolean questionRunPassFlag = true;
        for (List<InputItem> inputTestCase : inputTestCaseList){
            //测试用例是否通过
            boolean testCasePassFlag = true;
            //根据不用的判题模式获取执行代码请求
            Integer pattern = question.getPattern();
            JudgePatternEnum judgePatternEnum = JudgePatternEnum.getEnumByValue(pattern);
            if (judgePatternEnum == null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"判题模式不存在");
            }
            PatternContext patternContext = new PatternContext();
            patternContext.setPattern(judgePatternEnum.getText());
            patternContext.setCode(code);
            patternContext.setLanguage(language);
            patternContext.setInputTestCase(inputTestCase);
            patternContext.setQuestion(question);
            //获取测试代码沙箱执行请求
            ExecuteCodeRequest executeTestCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
            //获取测试代码沙箱执行结果
            ExecuteCodeResponse executeTestCodeResponse = codeSandBox.executeCode(executeTestCodeRequest);
            String correctCode = question.getCorrectCode();
            patternContext.setCode(correctCode);
            //获取正确代码沙箱执行请求
            ExecuteCodeRequest executeCorrectCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
            //获取正确代码沙箱执行结果
            ExecuteCodeResponse executeCorrectCodeResponse = codeSandBox.executeCode(executeCorrectCodeRequest);
            Integer executeResultCode = executeTestCodeResponse.getExecuteResultInfo().getCode();
            JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.getEnumByValue(executeResultCode);
            if (judgeInfoMessageEnum == null){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            if (!Objects.equals(executeResultCode, JudgeInfoMessageEnum.ACCEPTED.getValue())){
                questionRunVo.setCode(executeResultCode);
                questionRunVo.setMessage(JudgeInfoMessageEnum.getEnumByValue(executeResultCode).getMessage());
                questionRunVo.setErrorInfo(executeTestCodeResponse.getExecuteResultInfo().getErrorInfo());
                return questionRunVo;
            }
            //获取代码沙箱测试结果列表
            List<OutputItem> outputTestResult = executeTestCodeResponse.getOutputTestResult();
            //获取代码沙箱正确结果列表
            List<OutputItem> outputTestCorrectCodeResult = executeCorrectCodeResponse.getOutputTestResult();

            QuestionRunResultVo questionRunResultVo = new QuestionRunResultVo();
            //判断正确结果输出列表和测试结果输出列表元素是否相等
            if (outputTestCorrectCodeResult.size() != outputTestResult.size()){
                testCasePassFlag = false;
                questionRunPassFlag = false;
            }
            for (int i = 0; i < outputTestCorrectCodeResult.size(); i++) {
                OutputItem correctOutputItem = outputTestCorrectCodeResult.get(i);
                OutputItem testOutputItem = outputTestResult.get(i);
                //判断每一项正确输出结果名称和测试输出结果名称是否一致
                if (!correctOutputItem.getParamName().equals(testOutputItem.getParamName())){
                    testCasePassFlag = false;
                    questionRunPassFlag = false;
                }
                //判断每一项正确输出结果值和测试输出结果值是否一致
                if (!correctOutputItem.getParamValue().equals(testOutputItem.getParamValue())){
                    testCasePassFlag = false;
                    questionRunPassFlag = false;
                }
            }
            //测试用例是否通过
            if (testCasePassFlag){
                questionRunResultVo.setCode(JudgeInfoMessageEnum.PASS.getValue());
                questionRunResultVo.setMessage(JudgeInfoMessageEnum.PASS.getText());
            }else{
                questionRunResultVo.setCode(JudgeInfoMessageEnum.NO_PASS.getValue());
                questionRunResultVo.setMessage(JudgeInfoMessageEnum.NO_PASS.getText());
            }
            questionRunResultVo.setId(IdUtil.getSnowflakeNextId());
            questionRunResultVo.setExecuteTime(executeTestCodeResponse.getExecuteResultInfo().getTime());;
            questionRunResultVo.setInput(inputTestCase);
            questionRunResultVo.setOutput(outputTestResult);
            questionRunResultVo.setExpectOutput(outputTestCorrectCodeResult);
            questionRunResultVoList.add(questionRunResultVo);
        }


        //题目运行是否通过
        if (questionRunPassFlag){
            questionRunVo.setCode(JudgeInfoMessageEnum.PASS.getValue());
            questionRunVo.setMessage(JudgeInfoMessageEnum.PASS.getText());
        }else{
            questionRunVo.setCode(JudgeInfoMessageEnum.NO_PASS.getValue());
            questionRunVo.setMessage(JudgeInfoMessageEnum.NO_PASS.getText());
        }
        questionRunVo.setQuestionRunResultVoList(questionRunResultVoList);
        questionRunVo.setExecuteTime(100L);
        return questionRunVo;

    }
}
