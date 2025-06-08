package ai.momoyeyu.figspace.model.dto.figure;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UploadRequest implements Serializable {

    private Long id;

    @Serial
    private static final long serialVersionUID = -4771949453181150579L;

}
