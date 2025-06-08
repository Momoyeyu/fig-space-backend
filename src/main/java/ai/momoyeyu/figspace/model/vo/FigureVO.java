package ai.momoyeyu.figspace.model.vo;

import ai.momoyeyu.figspace.model.entity.Figure;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片
 */
@Data
public class FigureVO implements Serializable {
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
     * 标签数组
     */
    private List<String> tags;

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
     * 上传用户的信息
     */
    private UserVO userVO;

    @Serial
    private static final long serialVersionUID = 1L;

    public static Figure voToObj(FigureVO figureVO) {
        if (figureVO == null) {
            return null;
        }
        Figure figure = new Figure();
        BeanUtils.copyProperties(figureVO, figure);
        figure.setTags(JSONUtil.toJsonStr(figureVO.getTags()));
        return figure;
    }

    public static FigureVO objToVo(Figure figure) {
        if (figure == null) {
            return null;
        }
        FigureVO figureVO = new FigureVO();
        BeanUtils.copyProperties(figure, figureVO);
        figureVO.setTags(JSONUtil.toList(figure.getTags(), String.class));
        return figureVO;
    }
}