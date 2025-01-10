# 数据库初始化


-- 创建库
create database if not exists oj;

-- 切换库
use oj;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (unionId);

-- 题目表
create table if not exists question
(
    id          bigint auto_increment comment 'id'
        primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text                               null comment '题目答案',
    submitNum   int      default 0                 not null comment '题目提交数',
    acceptedNum int      default 0                 not null comment '题目通过数',
    judgeCase   text                               null comment '判题用例（json数组）',
    judgeConfig text                               null comment '判题配置（json对象）',
    thumbNum    int      default 0                 not null comment '点赞数',
    favourNum   int      default 0                 not null comment '收藏数',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    difficulty  tinyint  default 1                 not null,
    pattern     int      default 2                 not null comment '0-ACM,1-args,2-leetcode',
    initialCode text                               not null comment '初始代码（json 数组）',
    judgeCode   text                               not null comment '特判程序代码',
    passRate    decimal(5, 2) as (if((`submitNum` = 0), 0, ((`acceptedNum` / `submitNum`) * 100))) stored comment '通过率',
    correctCode text                               null comment '正确代码'
)
    comment '题目' collate = utf8mb4_unicode_ci;

create index idx_userId
    on question (userId);

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id'
        primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json对象）',
    status     int      default 0                 not null comment '判题状态（0 待判题、1 -判题中、2 -成功、3 -失败）',
    questionId bigint                             not null comment '题目id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '题目提交';

create index idx_questionId
    on question_submit (questionId);

create index idx_userId
    on question_submit (userId);


INSERT INTO oj.user (id, userAccount, userPassword, unionId, mpOpenId, userName, userAvatar, userProfile, userRole, createTime, updateTime, isDelete) VALUES (1877237668173623298, '1911354806@', '4697946e94f230a167943934c285fd55', null, null, 'OJ用户', 'https://oj-20250108.oss-cn-beijing.aliyuncs.com/avatar.jpg?Expires=1736360574&OSSAccessKeyId=TMP.3KkWkdTy1jHciEYA5BkPH4K2hkQsJDYRJBtwcSSgbmtepa51h2sVawpNgAoJFw3Zos3XZ7X1fGjbPYwFxWMDtmvvNMVY9H&Signature=1sPWvswDwI2qclEPhSU9O62bAd0%3D', null, 'admin', '2025-01-09 06:15:12', '2025-01-09 06:16:29', 0);

INSERT INTO oj.question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete, difficulty, pattern, initialCode, judgeCode, correctCode) VALUES (1877238419470422018, '两数之和', '给定一个整数数组 `nums` 和一个整数目标值 `target`，请你在该数组中找出 **和为目标值** `target` 的那 **两个** 整数，并返回它们的数组下标。

你可以假设每种输入只会对应一个答案，并且你不能使用两次相同的元素。

你可以按任意顺序返回答案。

**示例 1：**

> ```auto
> 输入：nums = [2,7,11,15], target = 9
> 输出：[0,1]
> 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
> ```

**示例2：**

> ```auto
> 输入：nums = [3,2,4], target = 6
> 输出：[1,2]
> ```

**示例 3：**

> ```auto
> 输入：nums = [3,3], target = 6
> 输出：[0,1]
> ```

**提示：**

-   `2 <= nums.length <= 104`

-   `-109 <= nums[i] <= 109`

-   `-109 <= target <= 109`

-   **只会存在一个有效答案**', '["数组","哈希表"]', '**方法一：暴力枚举**

**思路及算法**

最容易想到的方法是枚举数组中的每一个数 `x`，寻找数组中是否存在 `target - x`。

当我们使用遍历整个数组的方式寻找 `target - x` 时，需要注意到每一个位于 `x` 之前的元素都已经和 x 匹配过，因此不需要再进行匹配。而每一个元素不能被使用两次，所以我们只需要在 x 后面的元素中寻找 `target - x`。

**代码**

```auto
class Solution {
    public int[] twoSum(int[] nums, int target) {
        int n = nums.length;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[0];
    }
}
```

**复杂度分析**

-   时间复杂度：_O_(_N_2)，其中 _N_ 是数组中的元素数量。最坏情况下数组中任意两个数都要被匹配一次。

-   空间复杂度：_O_(1)。


**方法二：哈希表**

**思路及算法**

注意到方法一的时间复杂度较高的原因是寻找 `target - x` 的时间复杂度过高。因此，我们需要一种更优秀的方法，能够快速寻找数组中是否存在目标元素。如果存在，我们需要找出它的索引。

使用哈希表，可以将寻找 `target - x` 的时间复杂度降低到从 _O(N)_ 降低到 _O(1)_。

这样我们创建一个哈希表，对于每一个 `x`，我们首先查询哈希表中是否存在 `target - x`，然后将 `x` 插入到哈希表中，即可保证不会让 `x` 和自己匹配。

```auto
class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> hashtable = new HashMap<Integer, Integer>();
        for (int i = 0; i < nums.length; ++i) {
            if (hashtable.containsKey(target - nums[i])) {
                return new int[]{hashtable.get(target - nums[i]), i};
            }
            hashtable.put(nums[i], i);
        }
        return new int[0];
    }
}
```

**复杂度分析**

-   时间复杂度：O(N)，其中 N 是数组中的元素数量。对于每一个元素 x，我们可以 O(1) 地寻找 target - x。

-   空间复杂度：O(N)，其中 N 是数组中的元素数量。主要为哈希表的开销。', 0, 0, '[
  {
    "id": 1,
    "input": [
      {
        "paramName": "nums",
        "paramValue": "2 7 11 15"
      },
      {
        "paramName": "target",
        "paramValue": "9"
      }
    ],
    "output": [
      {
        "paramName": "result",
        "paramValue": "0 1"
      }
    ]
  },
  {
    "id": 2,
    "input": [
      {
        "paramName": "nums",
        "paramValue": "2 7 11 15"
      },
      {
        "paramName": "target",
        "paramValue": "9"
      }
    ],
    "output": [
      {
        "paramName": "result",
        "paramValue": "0 1"
      }
    ]
  }
]', '{"timeLimit":1000,"memoryLimit":500,"stackLimit":200}', 0, 0, 1877237668173623298, '2025-01-09 06:18:11', '2025-01-09 06:19:46', 0, 1, 2, 'class Solution {
    public int[] twoSum(int[] nums, int target) {

    }
}', 'public class Main
{
    public static void main(String args[]) throws Exception
    {
        Scanner cin=new Scanner(System.in);
        String arg1 = cin.nextLine();
        String[] arg1Array = arg1.split(":");
        String[] s1 = arg1Array[1].split(" ");
        String arg2 = cin.nextLine();
        String[] arg2Array = arg2.split(":");
        String s2 = arg2Array[1];
        Solution solution = new Solution();
        int [] nums = new int[s1.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i]= Integer.parseInt(s1[i]);
        }
        int[] ints = solution.twoSum(nums, Integer.parseInt(s2));
        for (int i = 0; i < ints.length; i++) {
            System.out.print(ints[i] + " ");
        }
    }
}', 'class Solution {
    public int[] twoSum(int[] nums, int target) {
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        for(int i = 0 ;i < nums.length;i++){
            if(hashMap.containsKey(target-nums[i])){
                return new int[]{hashMap.get(target-nums[i]),i};
            }
            hashMap.put(nums[i],i);
        }
        return new int[0];
    }
}');
