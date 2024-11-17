package com.ytb.model.codesandbox;

import com.ytb.model.dto.question.InputItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRequest {

    private List<List<InputItem>> inputTestCaseList;

    private String code;

    private String language;
}
