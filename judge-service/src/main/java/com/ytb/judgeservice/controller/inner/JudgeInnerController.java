package com.ytb.judgeservice.controller.inner;

import com.ytb.judgeservice.judge.JudgeService;
import com.ytb.model.dto.question.TestCase;
import com.ytb.model.dto.questionsubmit.QuestionRunRequest;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.model.vo.QuestionRunResultVo;
import com.ytb.model.vo.QuestionRunVo;
import com.ytb.serviceclient.JudgeFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @Override
    public QuestionRunVo doRun(@RequestBody QuestionRunRequest questionRunRequest) {
        return judgeService.doRun(questionRunRequest);
    }


}
