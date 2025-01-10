package com.ytb.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息信息枚举
 *

 */
public enum JudgeInfoMessageEnum {

    ACCEPTED("成功", 8001,"Accepted"),
    WRONG_ANSWER("答案错误", 8002,"Wrong Answer"),
    COMPILE_ERROR("编译错误", 8003,"compile error"),
    MEMORY_LIMIT_EXCEEDED("内存溢出",8004, "memory limit exceeded"),
    TIME_LIMIT_EXCEEDED("超时", 8005,"time limit exceeded"),
    PRESENTATION_ERROR("展示错误", 8006,"presentation error"),
    WAITING("等待中", 8007,"waiting"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出", 8008,"output limit exceeded"),
    DANGEROUS_OPERATION("危险操作", 8009,"dangerous operation"),
    RUNTIME_ERROR("运行错误", 8010,"runtime error"),
    PASS("通过", 8011,"pass"),
    NO_PASS("不通过", 8012,"no pass"),
    TESTCASE_ERROR("测试用例错误", 8013,"test case error"),
    SYSTEM_ERROR("系统错误", 8014,"system error");

    private final String text;

    private final Integer value;

    private final String message;

    JudgeInfoMessageEnum(String text, Integer value,String message) {
        this.text = text;
        this.value = value;
        this.message = message;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessageEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public String getMessage() {
        return message;
    }
}
