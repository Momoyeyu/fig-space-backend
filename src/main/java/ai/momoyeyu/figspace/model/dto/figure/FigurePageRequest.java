package ai.momoyeyu.figspace.model.dto.figure;

import ai.momoyeyu.figspace.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class FigurePageRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -914929236546368126L;

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";

}

