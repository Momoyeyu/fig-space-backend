package ai.momoyeyu.figspace.model.dto.figure;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FigureUploadRequest implements Serializable {

    /**
     * 图片ID（以支持修改图片）
     */
    private Long id;

    /**
     * 现有图片文件地址（用于支持通过URL方式上传图片）
     */
    private String fileUrl;

    /**
     * 指定图片名称
     */
    private String figName;

    @Serial
    private static final long serialVersionUID = -4771949453181150579L;

}
