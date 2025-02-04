package utc.englishlearning.Encybara.domain.request.review;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;

@Getter
@Setter
public class ReqCreateReviewDTO {
    private Long userId;
    private Long courseId;
    private String reContent;
    private String reSubject;
    private int numStar;
    private ReviewStatusEnum status;
}