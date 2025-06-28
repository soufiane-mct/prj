package com.aliboo.book.feedback;

import com.aliboo.book.book.Book;
import com.aliboo.book.book.BookRepository;
import com.aliboo.book.common.PageResponse;
import com.aliboo.book.exception.OperationNotPermitedException;
import com.aliboo.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;
    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        //hna andiro save method l feedback 3an tari9 connected user o data d kola var li aydkhl lina l user f FeedbackRequest

        //nt2kdo bli l book kyn o not archived o is sharble o myknsh l owner dl book howa li ay3ti feedback
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + request.bookId()));
        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermitedException("You cannot give a feedback for an archived or not shareable book");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermitedException("You cannot give feedback to your own book");
        }
        //lkn klshi mzn ead dir lia
        Feedback feedback = feedbackMapper.toFeedback(request);

        return feedbackRepository.save(feedback).getId();//3an tri9 repo jib l id o save l feedback
    }

    public PageResponse<FeedbackResponse> findAllFeedbacksByBook(Integer bookId, int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = ((User) connectedUser.getPrincipal());
        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable); //anjibo feedbach ka pages o mn bookid 3antari9 findAllByBookId lidrna f repo
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();

        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getSize(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );

    }
}
