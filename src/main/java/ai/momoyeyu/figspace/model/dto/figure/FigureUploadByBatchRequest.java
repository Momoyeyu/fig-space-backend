package ai.momoyeyu.figspace.model.dto.figure;

import lombok.Data;

@Data
public class FigureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 名称前缀
     */
    private String namePrefix;

    /**
     * 搜索数量（默认为10）
     */
    private Integer count = 10;

}
