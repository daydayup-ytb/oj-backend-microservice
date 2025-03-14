package com.ytb.questionservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ytb.common.common.ErrorCode;
import com.ytb.common.constant.CommonConstant;
import com.ytb.common.exception.BusinessException;
import com.ytb.common.exception.ThrowUtils;
import com.ytb.common.utils.SqlUtils;
import com.ytb.model.codesandbox.JudgeInfo;
import com.ytb.model.dto.question.QuestionQueryRequest;
import com.ytb.model.entity.Question;
import com.ytb.model.entity.QuestionSubmit;
import com.ytb.model.entity.User;
import com.ytb.model.vo.QuestionVO;
import com.ytb.model.vo.UserVO;
import com.ytb.questionservice.mapper.QuestionMapper;
import com.ytb.questionservice.mapper.QuestionSubmitMapper;
import com.ytb.questionservice.service.QuestionService;
import com.ytb.questionservice.service.QuestionSubmitService;
import com.ytb.serviceclient.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ytb19
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2024-02-22 20:58:12
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        List<String> tags = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        Integer difficulty = questionQueryRequest.getDifficulty();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(difficulty),"difficulty",difficulty);
        queryWrapper.like(StringUtils.isNotBlank(searchText),"title",searchText);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        long questionId = question.getId();
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> questionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> questionIdHasFavourMap = new HashMap<>();
        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userFeignClient.getUserVO(user));
            //设置用户做题状态
            int questionSubmitStatus = getQuestionSubmitStatus(question.getId(), request);
            if (questionSubmitStatus != -1) {
                questionVO.setState(questionSubmitStatus);
            }
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);

        return questionVOPage;
    }

    /**
     * 获取题库当前用户做题状态
     * @param questionId
     * @param request
     * @return
     */
    public int getQuestionSubmitStatus(long questionId, HttpServletRequest request) {
        User loginUser;
        try {
            loginUser = userFeignClient.getLoginUser(request);
        } catch (Exception e) {
            return -1;
        }
        Long userId = loginUser.getId();
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        List<QuestionSubmit> questionSubmitList = questionSubmitMapper.selectList(queryWrapper);
        // 未找到
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return 1;
        }
        for (QuestionSubmit questionSubmit : questionSubmitList) {
            String judgeInfoStr = questionSubmit.getJudgeInfo();
            JudgeInfo judgeInfo = JSONUtil.toBean(judgeInfoStr, JudgeInfo.class);
            String message = judgeInfo.getMessage();
            if (Objects.equals(message, "Accepted")) {
                return 3;
            }
        }
        return 2;
    }
}




