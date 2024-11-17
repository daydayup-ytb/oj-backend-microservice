package com.ytb.judgeservice.judge.strategy;

import com.ytb.model.dto.question.InputItem;
import com.ytb.model.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class PatternContext {

    private String pattern;

    private String code;

    private String language;

    private List<List<InputItem>> inputTestCaseList;

    private Question question;

}
