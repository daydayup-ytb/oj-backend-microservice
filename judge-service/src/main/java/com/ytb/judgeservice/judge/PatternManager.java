package com.ytb.judgeservice.judge;

import com.ytb.judgeservice.judge.strategy.AcmPatternStrategy;
import com.ytb.judgeservice.judge.strategy.CoreCodePatternStrategy;
import com.ytb.judgeservice.judge.strategy.PatternContext;
import com.ytb.judgeservice.judge.strategy.PatternStrategy;
import com.ytb.model.codesandbox.ExecuteCodeRequest;
import org.springframework.stereotype.Service;

@Service
public class PatternManager {

    /**
     * 执行判题
     * @param
     * @return
     */
    ExecuteCodeRequest getExecuteCodeRequest(PatternContext patternContext){
        String pattern = patternContext.getPattern();
        PatternStrategy patternStrategy = new AcmPatternStrategy();
        if ("ACM".equals(pattern)){
            patternStrategy = new AcmPatternStrategy();
        }else if ("CORE".equals(pattern)){
            patternStrategy = new CoreCodePatternStrategy();
        }
        return patternStrategy.getExecuteCodeRequest(patternContext);
    }
}
