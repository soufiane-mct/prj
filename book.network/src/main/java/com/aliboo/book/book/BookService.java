package com.aliboo.book.book;

import com.aliboo.book.common.PageResponse;
import com.aliboo.book.exception.OperationNotPermitedException;
import com.aliboo.book.file.FileStorageService;
import com.aliboo.book.history.BookTransactionHistory;
import com.aliboo.book.history.BookTransactionHistoryRepository;
import com.aliboo.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.aliboo.book.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository ;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {//connectedUser bhal var drna fiha Authentication ra deja drnaha f bookcontroller
        //hna anjibo user mn Authentication 3an tari9 userdetails o Principal li deja drna o endna fluser
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request); //bookMapper class li drna fiha l build ola save l vars d request o dirhom lina f var book
        book.setOwner(user); //l oner dl book howa luser li auth
        return bookRepository.save(book).getId();//hna andiro savel book f database dylna
    }

    public BookResponse findById(Integer bookId) {

        return bookRepository.findById(bookId)//jib id whd mn bookRepository
                .map(bookMapper::toBookResponse)//mn dk id book lijbti fo9 jib gae data dyalo li drna fl BookResponse 3antari9 toBookResponse li drnaha f bookMapper
                .orElseThrow(()-> new EntityNotFoundException("No book found with the Id" + bookId));
    }


    //fl PageResponse dir lia BookResponse (y3ni anjibo gae l books li kynin o an9smohom ela pages)
    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        Integer userId = null;
        if (connectedUser != null && connectedUser.getPrincipal() != null) {
            userId = ((User) connectedUser.getPrincipal()).getId();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books;
        if (userId != null) {
            books = bookRepository.findAllDisplayableBooks(pageable, userId);
        } else {
            books = bookRepository.findAllDisplayableBooksForGuests(pageable);
        }
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    //hna anjibo books by owner
    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()), (org.springframework.data.domain.Pageable) pageable); //filter books by user 3an tari9 BookSpecification li drna katjib lina l ow o an3tiwha hna l user id o l pageable o bch nkhdmo biha khas nzido f bookRepo extends d jpaspecificationExecutor<book>

        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();  //hdshi kmldiro f var bookResponse
        return new PageResponse<>(
                bookResponse,
                books.getNumber(), //number d page hdo endna fl PageResponse
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        ); //hdi bhl lwla li lfo9 ir hdi atjibhom byowner
    }

    //hdi bch njibo all borrowed books li eta l user
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks((org.springframework.data.domain.Pageable) pageable, user.getId()); //3an tari9 repo li drna jib lia mn BookTransactionHistory user li endo nfs l id libghina (l app fiha bzff user ms hna bghina l book d user whd dyalna li mno anjibo l allborrowed book dyalo)
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream() //BorrowedBookResponse drna fiha data fl bookmapper (hna dir var bookResponse type dyalha BorrowedBookResponse o dir fiha allBorrowedBooks lihia l var li drna fo9 o an3mroha l tht  )
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {

        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllReturnedBooks((org.springframework.data.domain.Pageable) pageable, user.getId()); //nfs blan l borrow andiro hna f returned drna hna findAllReturnedBooks f repo
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream() //BorrowedBookResponse drna fiha data fl bookmapper (hna dir var bookResponse type dyalha BorrowedBookResponse o dir fiha allBorrowedBooks lihia l var li drna fo9 o an3mroha l tht  )
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId)); // lmknsh dir hdshi lkn dr lia hdshi li ltht
        User user = ((User) connectedUser.getPrincipal());
        //only owner dl book ymkn ydir lih update
        if (!Objects.equals(book.getOwner().getId(), user.getId())) { //book owner id an9arnoh mea connected user id
            //lmakanosh metsawyin dir lia exeption
            throw new OperationNotPermitedException("You cannot update others books shareable status"); // OperationNotPermittedException dyalna drnaha fl exception file o kola exeption khas ndiro lih handel o drnah wl andiroh f globalexeptionHandler lfile dyalna
        }
        //lkn user connected howa mol ktab
        book.setShareable(!book.isShareable()); //hna kandiro inverse lvalue kn3eksoha bin true o false
        bookRepository.save(book); //moraha dir save
        return bookId; // hit l bookId hia l9ima lmkntsh aydir throw exeption ms lknt aydbl true b false

    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId)); // lmknsh dir hdshi lkn dr lia hdshi li ltht
        User user = ((User) connectedUser.getPrincipal());
        //only owner dl book ymkn ydir lih update
        if (!Objects.equals(book.getOwner().getId(), user.getId())) { //book owner id an9arnoh mea connected user id
            //lmakanosh metsawyin dir lia exeption
            throw new OperationNotPermitedException("You cannot update others books archived status"); // OperationNotPermittedException dyalna drnaha fl exception file o kola exeption khas ndiro lih handel o drnah wl andiroh f globalexeptionHandler lfile dyalna
        }
        //lkn user connected howa mol ktab
        book.setArchived(!book.isArchived()); //hna kandiro inverse lvalue kn3eksoha bin true o false
        bookRepository.save(book); //moraha dir save
        return bookId; // hit l bookId hia l9ima lmkntsh aydir throw exeption ms lknt aydbl true b false


    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId) //jib lia book id o diro f book
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId)); //mknsh book id drr hd err
        //anshofo l book khas ykon not archaved o sharbel bch ead n3tiwh l function d borrow
        if (book.isArchived() || !book.isShareable()){// || hadi or
            throw new OperationNotPermitedException("the requested book cannot be borrowed since it is archived or not shareable "); //lkn archived ola sharbel dir hd l err
        }
        //ola kn l id book sehih o mshi archived o shareable dir hdshi
        User user = ((User) connectedUser.getPrincipal()); //jib lia l connected user mn Authentication o diro f var user
        //hna andiro l book maykonsh dyal l owner (y3ni l connected user mydirsh borrow l ktab dyalo)
        if (Objects.equals(book.getOwner().getId(), user.getId())) { //book owner id an9arnoh mea connected user id lakn howa nit dir lia exeption
            throw new OperationNotPermitedException("You cannot borrow you own book"); // OperationNotPermittedException dyalna drnaha fl exception file o kola exeption khas ndiro lih handel o drnah wl andiroh f globalexeptionHandler lfile dyalna
        }
        //hna anshofo l book wsh already borrowed 3an tari9 isAlreadyBorrowedByUser lidrnaha f BookTransactionHistoryRepository interface
        final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if(isAlreadyBorrowed) {
            throw new OperationNotPermitedException("The requested book is already borrowed");
        }
        //lmknosh gae dok l exeption dir lia build l BookTransactionHistory b data li endna user connected user o book lifih book id o dir false l returned o returnApproved
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        //drna false y3ni l book drna lih borrow mahedo deja mekri y3ni ba9i marj3 y3ni returned(false) o returnApproved(false)
        return transactionHistoryRepository.save(bookTransactionHistory).getId(); //id d transaction shd l vars d bookTransactionHistory o 3mrhom b l9iyam li drna l fo9 3antari9 BookTransactionHistoryRepository
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        //njibo l book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
        //mykonsh archived
        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermitedException("the requested book cannot be borrowed since it is archived or not shareable ");
        }
        //l user mydirsh retern l book dyalo (connected user)
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermitedException("You cannot return your own book");
        }
        //nt2kdo bli l user deja borrowa l book bch ymkn yrj3o mnb3d
        List<BookTransactionHistory> transactions = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId());
        if (transactions.isEmpty()) {
            throw new OperationNotPermitedException("You did not borrow this Book");
        }
        BookTransactionHistory bookTransactionHistory = transactions.get(0); // Get the most recent transaction
        //lmkn ta if mn hdshi li lfo9 ead dir returned borrowed book
        bookTransactionHistory.setReturned(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }



    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        //nfs blan d returnBorrowedBook

        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermitedException("the requested book cannot be borrowed since it is archived or not shareable ");
        }

        User user = ((User) connectedUser.getPrincipal());
        //Only the book owner can approve the return
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermitedException("Only the book owner can approve the return");
        }
        //f returnBorrowedBook drrna UserId hna khas ndiro l OwnerId his khas ykon l connected user howa l owner dl book
        List<BookTransactionHistory> transactions = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId());
        if (transactions.isEmpty()) {
            throw new OperationNotPermitedException("The book is not returned yet. You cannot approve its return ");
        }
        BookTransactionHistory bookTransactionHistory = transactions.get(0); // Get the most recent transaction
        bookTransactionHistory.setReturnApproved(true);

        return transactionHistoryRepository.save(bookTransactionHistory).getId() ;
    }


    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        //hna andiro service akher li ay3wna bch ndiro upload l file
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file, user.getId());//f kola user dir lia folder li andir fih upload all file dyal user
        book.setBookCover(bookCover);
        bookRepository.save(book);

    }

    public Book getBookEntityById(Integer bookId) {
        return bookRepository.findById(bookId).orElse(null);
    }

}
