package com.aliboo.book.feedback;

import com.aliboo.book.book.Book;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeedbackMapper {
    public Feedback toFeedback(FeedbackRequest request) {

        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .build()) //jib lia book id li andiro lih feedback tahwa
                .build();
    }

    public FeedbackResponse toFeedbackResponse(Feedback feedback, Integer id) {

        return FeedbackResponse.builder()
                .note(feedback.getNote())
                .comment(feedback.getComment())
                .ownFeedback(Objects.equals(feedback.getCratedBy(), id))
                .build();
    }
}
