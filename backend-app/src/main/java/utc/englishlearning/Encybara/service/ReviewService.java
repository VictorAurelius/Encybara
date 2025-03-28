package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.request.review.ReqCreateReviewDTO;
import utc.englishlearning.Encybara.domain.response.review.ResReviewDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.exception.ResourceAlreadyExistsException;
import utc.englishlearning.Encybara.repository.ReviewRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;
import utc.englishlearning.Encybara.repository.CourseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Objects;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private NotificationService notificationService;

    private static final double MINIMUM_COMPLETION_LEVEL = 30.0; // Minimum 30% completion to review

    @Transactional
    public ResReviewDTO createReview(ReqCreateReviewDTO reqCreateReviewDTO) {
        // Check if user has already reviewed this course
        if (reviewRepository.existsByUserIdAndCourseId(reqCreateReviewDTO.getUserId(),
                reqCreateReviewDTO.getCourseId())) {
            throw new ResourceAlreadyExistsException("User has already reviewed this course.");
        }

        // Get enrollment to validate user's progress
        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseId(reqCreateReviewDTO.getUserId(), reqCreateReviewDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not enrolled in this course"));

        // Validate enrollment completion
        if (enrollment.getComLevel() < MINIMUM_COMPLETION_LEVEL) {
            throw new IllegalStateException(
                    String.format("Must complete at least %.0f%% of the course to review", MINIMUM_COMPLETION_LEVEL));
        }

        Review review = new Review();
        review.setUser(userRepository.findById(reqCreateReviewDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        review.setCourse(courseRepository.findById(reqCreateReviewDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found")));
        review.setReContent(reqCreateReviewDTO.getReContent());
        review.setReSubject(reqCreateReviewDTO.getReSubject());
        review.setNumStar(reqCreateReviewDTO.getNumStar());
        review.setNumLike(0);
        review.setStatus(reqCreateReviewDTO.getStatus());

        review = reviewRepository.save(review);

        // Send notification
        notificationService.createNotificationForReview(review);

        return convertToDTO(review);
    }

    @Transactional
    public ResReviewDTO updateReview(Long id, Long userId, ReqCreateReviewDTO reqUpdateReviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new ResourceNotFoundException("User ID does not match the review owner.");
        }

        // Validate enrollment completion again in case the course was reset
        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseId(userId, review.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not enrolled in this course"));

        if (enrollment.getComLevel() < MINIMUM_COMPLETION_LEVEL) {
            throw new IllegalStateException(
                    String.format("Must complete at least %.0f%% of the course to maintain review",
                            MINIMUM_COMPLETION_LEVEL));
        }

        review.setReContent(reqUpdateReviewDTO.getReContent());
        review.setReSubject(reqUpdateReviewDTO.getReSubject());
        review.setNumStar(reqUpdateReviewDTO.getNumStar());
        review.setStatus(reqUpdateReviewDTO.getStatus());
        reviewRepository.save(review);

        // Send notification
        notificationService.createNotificationForReview(review);

        return convertToDTO(review);
    }

    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new ResourceNotFoundException("User ID does not match the review owner.");
        }

        reviewRepository.delete(review);
    }

    public Page<ResReviewDTO> getAllReviewsByCourseId(Long courseId, Pageable pageable, Integer numStar,
            ReviewStatusEnum status) {
        Page<Review> reviews;
        if (numStar != null && status != null) {
            reviews = reviewRepository.findByCourseIdAndNumStarAndStatus(courseId, numStar, status, pageable);
        } else {
            reviews = reviewRepository.findByCourseId(courseId, pageable);
        }
        return reviews.map(this::convertToDTO);
    }

    public Page<ResReviewDTO> getAllReviewsByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable).map(this::convertToDTO);
    }

    private ResReviewDTO convertToDTO(Review review) {
        ResReviewDTO dto = new ResReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setCourseId(review.getCourse().getId());
        dto.setReContent(review.getReContent());
        dto.setReSubject(review.getReSubject());
        dto.setNumStar(review.getNumStar());
        dto.setNumLike(review.getNumLike());
        dto.setStatus(review.getStatus());
        return dto;
    }
}