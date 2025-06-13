package ai.momoyeyu.figspace.model.dto.figure;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class FigureQueryRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 搜索词
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    @Serial
    private static final long serialVersionUID = -8089126954078596189L;
}