package ai.momoyeyu.figspace.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图片
 * @TableName figure
 */
@TableName(value ="figure")
@Data
public class Figure implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 url 地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 介绍
     */
    private String intro;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签：JSON数组
     */
    private String tags;

    /**
     * 图片大小
     */
    private Long figSize;

    /**
     * 图片宽度
     */
    private Integer figWidth;

    /**
     * 图片高度
     */
    private Integer figHeight;

    /**
     * 图片宽高比
     */
    private Double figScale;

    /**
     * 图片格式
     */
    private String figFormat;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间（用户通过系统的编辑时间）
     */
    private Date editTime;

    /**
     * 更新时间（包括了直接改数据库）
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = -8089126954078596189L;
}