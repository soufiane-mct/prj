package com.aliboo.book.feedback;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class FeedbackResponse {
    private Double note;
    private String comment;
    private boolean ownFeedback;//lkn user howa li ay3ti l feedback ndiro lih lfeedback dyalo bshi lone akher (highlight it)

}
