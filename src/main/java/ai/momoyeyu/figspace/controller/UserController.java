package ai.momoyeyu.figspace.controller;

import ai.momoyeyu.figspace.annotation.AuthCheck;
import ai.momoyeyu.figspace.common.BaseResponse;
import ai.momoyeyu.figspace.common.DeleteRequest;
import ai.momoyeyu.figspace.common.ResultUtils;
import ai.momoyeyu.figspace.constant.UserConstant;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.model.dto.user.*;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.LoginUserVO;
import ai.momoyeyu.figspace.model.vo.UserVO;
import ai.momoyeyu.figspace.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @return 用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userCheckPassword = userRegisterRequest.getCheckPassword();
        return ResultUtils.success(userService.userRegister(userAccount, userPassword, userCheckPassword));
    }

    /**
     * 用户登录
     * @param userLoginRequest 登录请求DTO
     * @param request request
     * @return 用户VO
     */
    @PostMapping("/login")
    @Parameter(name = "userAccount", example = "momoyeyu")
    @Parameter(name = "userPassword", example = "12345678")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request, HttpServletResponse response) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtils.success(userService.userLogin(userAccount, userPassword, request, response));
    }

    /**
     * 用户退出登录
     * @param request request
     * @return 退出是否成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 管理员添加用户（默认密码：12345678）
     * @param userAddRequest 添加用户DTO
     * @return 用户ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 1. 新建用户对象，拷贝属性
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 2. 设置默认密码
        final String defaultPassword = "12345678";
        String encryptedPassword = userService.getEncryptPassword(defaultPassword);
        user.setUserPassword(encryptedPassword);
        // 3. 保存新用户
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "database error");
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户（仅管理员可用）
     * @param deleteRequest 删除DTO（公共模版）
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "user not exist");
        return ResultUtils.success(true);
    }

    /**
     * 更新用户信息（仅管理员可用）
     * @param userUpdateRequest 更新信息
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 1. check null
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        // 2. find user instance
        User user = userService.getById(userUpdateRequest.getId());
        // 3. update user info
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "database error");
        return ResultUtils.success(true);
    }

    /**
     * 按页查询用户数据（仅管理员可用）
     * @param userQueryRequest 查询请求
     * @return 分页的用户数据
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int pagesize = userQueryRequest.getPageSize();
        // 1. 按页查询
        Page<User> userPage = userService.page(new Page<>(current, pagesize),
                userService.getQueryWrapper(userQueryRequest));
        // 2. 数据脱敏
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        // 3. 转换回页
        Page<UserVO> userVOPage = new Page<>(current, pagesize, userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
