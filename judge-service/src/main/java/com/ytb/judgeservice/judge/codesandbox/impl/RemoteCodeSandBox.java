package com.ytb.judgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ytb.common.common.ErrorCode;
import com.ytb.common.exception.BusinessException;
import com.ytb.judgeservice.judge.codesandbox.CodeSandBox;
import com.ytb.model.codesandbox.ExecuteCodeRequest;
import com.ytb.model.codesandbox.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
@Slf4j
public class RemoteCodeSandBox implements CodeSandBox {

    //定义鉴权请求头
    public static final String AUTH_REQUEST_HEADER = "AUTH";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    private static final String url = "http://111.231.5.19:8090/executeCode";
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("调用远程代码沙箱");
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url).header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET).body(json).execute().body();
        if (StringUtils.isBlank(responseStr)) {
            log.error("executeCode remoteSandbox error,message = {}" + responseStr);
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error,message = {}" + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
