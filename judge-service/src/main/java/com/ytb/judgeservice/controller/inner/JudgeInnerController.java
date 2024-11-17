package com.ytb.judgeservice.controller.inner;

import com.ytb.judgeservice.judge.JudgeService;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.serviceclient.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    JudgeService judgeService;

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    @Override
    @PostMapping("/do")
    public QuestionSubmit doJudge(@RequestParam(value = "questionSubmitId") long questionSubmitId){
        return judgeService.doJudge(questionSubmitId);
    }
}
