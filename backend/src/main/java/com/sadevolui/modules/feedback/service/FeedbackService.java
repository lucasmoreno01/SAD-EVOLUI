package com.sadevolui.modules.feedback.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.feedback.dto.FeedbackRequest;
import com.sadevolui.modules.feedback.dto.FeedbackResponse;
import com.sadevolui.modules.feedback.entity.Feedback;
import com.sadevolui.modules.feedback.entity.FeedbackSource;
import com.sadevolui.modules.feedback.repository.FeedbackRepository;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.ConflictException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> list(UUID applicationId, UUID ownerId) {
        List<Feedback> feedbacks = applicationId == null
                ? feedbackRepository.findAllByApplication_Project_Owner_IdOrderBySubmittedAtDesc(ownerId)
                : feedbackRepository.findAllByApplication_IdAndApplication_Project_Owner_IdOrderBySubmittedAtDesc(applicationId, ownerId);
        return feedbacks.stream().map(FeedbackService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse get(UUID id, UUID ownerId) {
        return toResponse(findFeedback(id, ownerId));
    }

    public FeedbackResponse create(FeedbackRequest request, UUID ownerId) {
        UserAccount creator = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Usuário", ownerId));
        return createForApplication(request, findApplication(request.applicationId(), ownerId), creator, FeedbackSource.MANUAL);
    }

    public FeedbackResponse createExternal(FeedbackRequest request) {
        return createForApplication(request, findApplication(request.applicationId(), null), null, FeedbackSource.EXTERNAL);
    }

    private FeedbackResponse createForApplication(FeedbackRequest request, ManagedApplication application, UserAccount creator, FeedbackSource source) {
        String comment = request.comment().trim();
        if (feedbackRepository.existsByApplicationIdAndRatingAndCommentAndSubmittedAt(
                application.getId(), (short) request.rating(), comment, request.submittedAt())) {
            throw new ConflictException("Este feedback já está registrado para o aplicativo e a data informados.");
        }
        return toResponse(feedbackRepository.save(new Feedback(application, creator, source, request.rating(), comment, request.submittedAt())));
    }

    public FeedbackResponse update(UUID id, FeedbackRequest request, UUID ownerId) {
        Feedback feedback = findFeedback(id, ownerId);
        ManagedApplication application = findApplication(request.applicationId(), ownerId);
        String comment = request.comment().trim();
        if (!feedback.getApplication().getId().equals(application.getId())
                || feedback.getRating() != request.rating()
                || !feedback.getComment().equals(comment)
                || !feedback.getSubmittedAt().equals(request.submittedAt())) {
            if (feedbackRepository.existsByApplicationIdAndRatingAndCommentAndSubmittedAt(
                    application.getId(), (short) request.rating(), comment, request.submittedAt())) {
                throw new ConflictException("Este feedback já está registrado para o aplicativo e a data informados.");
            }
        }
        feedback.update(application, request.rating(), comment, request.submittedAt());
        return toResponse(feedbackRepository.save(feedback));
    }

    public void delete(UUID id, UUID ownerId) {
        feedbackRepository.delete(findFeedback(id, ownerId));
    }

    private Feedback findFeedback(UUID id, UUID ownerId) {
        return feedbackRepository.findByIdAndApplication_Project_Owner_Id(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
    }

    private ManagedApplication findApplication(UUID id, UUID ownerId) {
        return (ownerId == null ? applicationRepository.findById(id) : applicationRepository.findByIdAndProject_Owner_Id(id, ownerId))
                .orElseThrow(() -> new ResourceNotFoundException("Aplicativo", id));
    }

    private static FeedbackResponse toResponse(Feedback feedback) {
        ManagedApplication application = feedback.getApplication();
        return new FeedbackResponse(feedback.getId(), application.getId(), application.getName(), application.getProject().getId(), application.getProject().getName(), feedback.getRating(), feedback.getComment(), feedback.getSubmittedAt(), feedback.getCreatedAt(), feedback.getSource().name());
    }
}
