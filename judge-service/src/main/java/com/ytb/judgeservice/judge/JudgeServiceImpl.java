package com.ytb.judgeservice.judge;

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
        //1.传入题目的提交 id。获取相对应的题目、提交信息（包含代码、编程语言等）
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
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        //获取输入用例
        String judgeCaseStr = question.getJudgeCase();
//        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<TestCase> testCaseList = JSONUtil.toList(judgeCaseStr, TestCase.class);
        List<List<InputItem>> inputTestCaseList = testCaseList.stream().map(TestCase::getInput).collect(Collectors.toList());
//        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
//                .code(code)
//                .language(language)
//                .inputList(inputList)
//                .build();
        //根据不用的判题模式获取执行代码请求
        PatternContext patternContext = new PatternContext();
        //TODO 根据查询出来的题目信息，设置pattern 此处默认acm
        patternContext.setPattern("CORE");
        patternContext.setCode(code);
        patternContext.setLanguage(language);
        patternContext.setInputTestCaseList(inputTestCaseList);
        patternContext.setQuestion(question);
        ExecuteCodeRequest executeCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        List<List<OutputItem>> outputTestResultList = executeCodeResponse.getOutputTestResultList();
        //根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputTestCaseList(inputTestCaseList);
        judgeContext.setOutputTestResultList(outputTestResultList);
        judgeContext.setTestCaseList(testCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        //修改数据库中的判题结果
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
        return questionSubmitResult;
    }

    @Override
    public QuestionRunVo doRun(QuestionRunRequest questionRunRequest) {
        //1.传入题目的提交 id。获取相对应的题目、提交信息（包含代码、编程语言等）
        Long questionId = questionRunRequest.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        String language = questionRunRequest.getLanguage();
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
        if (testCaseList.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"测试用例为空");
        }
        // 调用沙箱 获取执行结果
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        codeSandBox = new CodeSandBoxProxy(codeSandBox);
        //获取输入用例
        List<List<InputItem>> inputTestCaseList = testCaseList.stream().map(TestCase::getInput).collect(Collectors.toList());
        List<QuestionRunResultVo> questionRunResultVoList = new ArrayList<>();
        for (List<InputItem> inputTestCase : inputTestCaseList){
            //根据不用的判题模式获取执行代码请求
            PatternContext patternContext = new PatternContext();
            patternContext.setPattern("CORE");
            patternContext.setCode(code);
            patternContext.setLanguage(language);
            patternContext.setInputTestCase(inputTestCase);
            patternContext.setQuestion(question);
            ExecuteCodeRequest executeCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
            ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
            List<OutputItem> outputTestResult = executeCodeResponse.getOutputTestResult();
            String correctCode = question.getCorrectCode();
            patternContext.setCode(correctCode);
            ExecuteCodeRequest executeCorrectCodeRequest = patternManager.getExecuteCodeRequest(patternContext);
            ExecuteCodeResponse executeCorrectCodeResponse = codeSandBox.executeCode(executeCorrectCodeRequest);
            List<OutputItem> outputTestCorrectCodeResult = executeCorrectCodeResponse.getOutputTestResult();
            QuestionRunResultVo questionRunResultVo = new QuestionRunResultVo();
            questionRunResultVo.setExecuteTime(executeCodeResponse.getJudgeInfo().getTime());
            questionRunResultVo.setInput(inputTestCase);
            questionRunResultVo.setOutput(outputTestResult);
            questionRunResultVo.setExpectOutput(outputTestCorrectCodeResult);
            questionRunResultVoList.add(questionRunResultVo);
        }
        QuestionRunVo questionRunVo = new QuestionRunVo();
        questionRunVo.setCode(1);
        questionRunVo.setMessage("通过");
        questionRunVo.setQuestionRunResultVoList(questionRunResultVoList);
        questionRunVo.setExecuteTime(100L);
        return questionRunVo;

    }
}
