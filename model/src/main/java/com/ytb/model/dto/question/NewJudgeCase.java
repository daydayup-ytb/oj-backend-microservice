package com.ytb.model.dto.question;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NewJudgeCase {

    private Map<String,Object> input;

    private Map<String,Object> output;
}
