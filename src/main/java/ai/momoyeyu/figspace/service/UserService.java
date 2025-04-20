package ai.momoyeyu.figspace.service;

import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

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
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

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
     * 密码加盐加密
     * @param password 密码
     * @return 加盐加密密码
     */
    String getEncryptPassword(String password);
}
