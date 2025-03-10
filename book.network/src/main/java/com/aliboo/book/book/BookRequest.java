package com.aliboo.book.book;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BookRequest(//madrnash class eadia drnaha record o record ra ir type an3rfo fih hdshi li ltht
              Integer id,//hna la kn l id null ancreyiw new book ladeja kn andiro update
              @NotNull(message = "100") //lkn null no3iya dl msg li at3tina lihowa r9m 100 (hd new3iya dl msg aynf3ona fl frontend ankhdmo bihom (mataln hna la tl3 100 angolih title is empty kn matalan 101 ra autherName empty..))
              @NotEmpty(message = "100")
              String title,

              @NotNull(message = "101")
              @NotEmpty(message = "101")
              String authorName,

              @NotNull(message = "102")
              @NotEmpty(message = "102")
              String isbn,

              @NotNull(message = "103")
              @NotEmpty(message = "103")
              String synopsis,

              boolean shareable
) {
}
