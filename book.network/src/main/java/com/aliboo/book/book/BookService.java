package com.aliboo.book.book;

import com.aliboo.book.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    public Integer save(BookRequest request, Authentication connectedUser) {//connectedUser bhal var drna fiha Authentication ra deja drnaha f bookcontroller
        //hna anjibo user mn Authentication 3an tari9 userdetails o Principal li deja drna o endna fluser
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request); //bookMapper class li drna fiha l build ola save l vars d request o dirhom lina f var book
        book.setOwner(user); //l oner dl book howa luser li auth
        return bookRepository.save(book).getId();//hna andiro savel book f database dylna
    }
}
