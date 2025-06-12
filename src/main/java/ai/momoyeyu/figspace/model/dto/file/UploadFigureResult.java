package ai.momoyeyu.figspace.model.dto.file;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UploadFigureResult implements Serializable {

    private String url;

    private String name;

    private Long figSize;

    private int figWidth;

    private int figHeight;

    /**
     * 图片宽高比
     */
    private Double figScale;

    private String figFormat;

    @Serial
    private static final long serialVersionUID = 1132021859273414219L;
}
