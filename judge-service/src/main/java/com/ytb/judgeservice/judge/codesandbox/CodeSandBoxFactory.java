package com.ytb.judgeservice.judge.codesandbox;


import com.ytb.judgeservice.judge.codesandbox.impl.RemoteCodeSandBox;
import com.ytb.judgeservice.judge.codesandbox.impl.ThirdPartyCodeSandBox;

/**
 * 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例）
 */
public class CodeSandBoxFactory {

    /**
     * 创建代码沙箱实例
     * @param type 沙箱实例
     * @return
     */
    public static CodeSandBox newInstance(String type){
        switch(type){
            case "remote":
                return new RemoteCodeSandBox();
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            default:
                return new RemoteCodeSandBox();
        }
    }
}
