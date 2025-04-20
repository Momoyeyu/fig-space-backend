package ai.momoyeyu.figspace.model.dto.user;

import ai.momoyeyu.figspace.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4898175248129959936L;

    private Long id;

    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
}
