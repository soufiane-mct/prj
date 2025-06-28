package com.aliboo.book.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
        @Positive(message = "200") //lmknsh note possitive tl3 lia msg 200 f postman mkhsh tkon note -1.. ola r9m salib
        @Min(value = 0, message = "201")
        @Max(value = 5, message = "202")
        Double note, //y3ni t9yim mbin 0 o 5 knt shi haja khra tl3 lia msg d kola whda ya 200 ya 201 ya 202
        @NotNull(message = "203")
        @NotEmpty(message = "203")
        @NotBlank(message = "203") //tl3 lia nfs err msg lmth9osh hd 3
        String comment,
        @NotNull(message = "204")
        Integer bookId
) {
}
