package ai.momoyeyu.figspace.service;

import ai.momoyeyu.figspace.model.dto.user.UserQueryRequest;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.LoginUserVO;
import ai.momoyeyu.figspace.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* @author Momoyeyu
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-04-20 17:18:16
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 请求
     * @return 用户VO
     */
    LoginUserVO userLogin(String userAccount, String userPassword,
                          HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取用户VO
     * @param loginUser 用户实例
     * @return 用户VO
     */
    LoginUserVO getLoginUserVO(User loginUser);

    /**
     * 获取登录用户的实例
     * @param request 请求
     * @return 用户实例
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     * @param request 请求
     * @return 是否退出成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * @param user 用户实例
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 数据脱敏：获取脱敏后的用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);


    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 密码加盐加密
     * @param password 密码
     * @return 加盐加密密码
     */
    String getEncryptPassword(String password);

    /**
     * 验证用户是否是管理员
     * @param user 用户信息
     * @return 验证结果
     */
    boolean isAdmin(User user);
}
