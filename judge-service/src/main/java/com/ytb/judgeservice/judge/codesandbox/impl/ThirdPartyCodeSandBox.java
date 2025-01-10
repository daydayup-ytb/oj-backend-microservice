package com.ytb.judgeservice.judge.codesandbox.impl;


import com.ytb.judgeservice.judge.codesandbox.CodeSandBox;
import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.codesandbox.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
@Slf4j
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("调用第三方代码沙箱");
        return null;
    }
}
