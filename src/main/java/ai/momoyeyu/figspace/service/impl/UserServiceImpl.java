package ai.momoyeyu.figspace.service.impl;

import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.model.dto.user.UserQueryRequest;
import ai.momoyeyu.figspace.model.vo.LoginUserVO;
import ai.momoyeyu.figspace.model.vo.UserVO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.service.UserService;
import ai.momoyeyu.figspace.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ai.momoyeyu.figspace.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Momoyeyu
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-04-20 17:18:16
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 检查输入合法性
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "password too short");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "two passwords do not match");

        // 2. 检查是否重复（用户账号已存在）
        boolean exist = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .exists();
        ThrowUtils.throwIf(exist, ErrorCode.OPERATION_ERROR, "account already used");

        // 3. 密码加密
        String encryptedPassword = getEncryptPassword(userPassword);

        // 4. 插入新用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("Noname"); // by default
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "DATABASE ERROR: fail to register user");
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 输入校验
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "account or password is blank");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "password too short");

        // 2. 密码加密
        String encryptedPassword = getEncryptPassword(userPassword);

        // 3. 用户查询
        User loginUser = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptedPassword)
                .one();
        if (loginUser == null) {
            // 日志尽量用英文：减小开销
            log.error("user login fail: userAccount can not match userPassword");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "wrong account or password");
        }
        // 4. 记录用户登录状态（Session）
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);

        // 5. 返回用户
        return this.getLoginUserVO(loginUser);
    }

    @Override
    public LoginUserVO getLoginUserVO(User loginUser) {
        if (loginUser == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        // Spring 实现的方法，将对应属性拷贝
        BeanUtils.copyProperties(loginUser, loginUserVO);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        currentUser = this.getById(currentUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1. 判断是否已登录
        ThrowUtils.throwIf(request.getSession().getAttribute(USER_LOGIN_STATE) == null,
                ErrorCode.NOT_LOGIN_ERROR);
        // 2. 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 判空
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 构建 Query
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 精确匹配
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        // 模糊匹配
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        // 根据某个 Field 进行排列
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    @Override
    public String getEncryptPassword(String password) {
        final String salt = "momoyeyu";
        return DigestUtils.md5DigestAsHex((password + salt).getBytes());
    }
}




