package ai.momoyeyu.figspace.model.enums;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum FigureReviewStatus {

    REVIEW("待审核", 0),
    ACCEPT("通过", 1),
    REJECT("拒绝", 2);

    private final String text;

    private final int value;

    private static final Map<Integer, FigureReviewStatus> map = new HashMap<>();

    static {
        for (FigureReviewStatus status : FigureReviewStatus.values()) {
            map.put(status.getValue(), status);
        }
    }

    FigureReviewStatus(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static FigureReviewStatus fromValue(int value) {
        return map.get(value);
    }

}
