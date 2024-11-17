package com.ytb.userservice.controller.inner;

import com.ytb.model.entity.User;
import com.ytb.serviceclient.UserFeignClient;
import com.ytb.userservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 该服务仅内部调用
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    UserService userService;

    /**
     * 根据id获取用户
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam(value = "userId") long userId){
        return userService.getById(userId);
    }

    /**
     * 根据id列表获取用户列表
     * @param idList
     * @return
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam(value = "idList") Collection<Long> idList){
        return userService.listByIds(idList);
    }

}
