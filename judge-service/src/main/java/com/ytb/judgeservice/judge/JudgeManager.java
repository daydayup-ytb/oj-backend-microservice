package com.ytb.judgeservice.judge;


import com.ytb.judgeservice.judge.strategy.DefaultJudgeStrategy;
import com.ytb.judgeservice.judge.strategy.JavaJudgeStrategy;
import com.ytb.judgeservice.judge.strategy.JudgeContext;
import com.ytb.judgeservice.judge.strategy.JudgeStrategy;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)){
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
