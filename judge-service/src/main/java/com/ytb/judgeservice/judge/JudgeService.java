package com.ytb.judgeservice.judge;


import com.ytb.model.dto.questionsubmit.QuestionRunRequest;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.model.vo.QuestionRunVo;

import java.util.List;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);

    QuestionRunVo doRun(QuestionRunRequest questionRunRequest);
}
