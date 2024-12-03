package com.ytb.serviceclient;


import com.ytb.model.dto.question.TestCase;
import com.ytb.model.dto.questionsubmit.QuestionRunRequest;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.model.vo.QuestionRunResultVo;
import com.ytb.model.vo.QuestionRunVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 判题服务
 */
@FeignClient(name = "oj-backend-judge-service",path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam(value = "questionSubmitId") long questionSubmitId);


    @PostMapping("/doRun")
    QuestionRunVo doRun(@RequestBody QuestionRunRequest questionRunRequest);
}
