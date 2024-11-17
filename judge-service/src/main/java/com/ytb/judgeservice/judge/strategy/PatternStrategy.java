package com.ytb.judgeservice.judge.strategy;

import com.ytb.model.codesandbox.ExecuteCodeRequest;

public interface PatternStrategy {

    ExecuteCodeRequest getExecuteCodeRequest(PatternContext patternContext);
}
