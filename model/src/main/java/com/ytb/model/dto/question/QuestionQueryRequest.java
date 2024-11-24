package com.ytb.model.dto.question;

import com.ytb.common.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 语言
     */
    private String language;

    /**
     * 难度
     */

    private Integer difficulty;

    /**
     * 当前用户解题状态
     */
    private Integer state;

    /**
     * 查询文本域
     */
    private String searchText;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}