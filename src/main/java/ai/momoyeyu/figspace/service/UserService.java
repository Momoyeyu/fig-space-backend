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

    Long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User loginUser);

    String getEncryptPassword(String password);
}
