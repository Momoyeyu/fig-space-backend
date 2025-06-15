package ai.momoyeyu.figspace.model.dto.figure;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class FigureReviewRequest implements Serializable {
  
    /**  
     * 图片id
     */  
    private Long id;  
  
    /**  
     * 状态：0-待审核, 1-通过, 2-拒绝  
     */  
    private Integer reviewStatus;  
  
    /**  
     * 审核信息  
     */  
    private String reviewMessage;  
  
    @Serial
    private static final long serialVersionUID = 1L;  
}
