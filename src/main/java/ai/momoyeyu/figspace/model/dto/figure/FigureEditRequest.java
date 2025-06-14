package ai.momoyeyu.figspace.model.dto.figure;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 【管理员】图片更新
 */
@Data
public class FigureEditRequest implements Serializable {

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
    private String tags;

    @Serial
    private static final long serialVersionUID = -8089126954078596189L;
}