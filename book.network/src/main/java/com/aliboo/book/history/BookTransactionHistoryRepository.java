package com.aliboo.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;


public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {

    //hdi bch nshofo l borrowedbook
    @Query("""
            SELECT history 
            FROM BookTransactionHistory history
            WHERE history.user.id = :userId
            """) // dir lia BookTransactionHistory f var history o mnha jib lia user.id li endo nfs l9ima dl userId dyalna li drna ltht (bhala l user.id li endna f data base arbtoha f var userId o hd l var ankhdmo biha ltht o f server)
    // bch flkhr mor mn khdmo b server atwli ( anjibo all borrowed book dyal l user mn data 3an tari9 BookTransactionHistory)
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, Integer userId);

    //nfs blan bch nshofo all retrnedbook li rej3o l user
    @Query("""
            SELECT history 
            FROM BookTransactionHistory history
            WHERE history.book.owner.id = :userId
            """)
    //y3ni mn mor mnkhdmo f server atwli  anjibo all retrned book dyal l owner o arbtoha f var userId li ankhdmo biha f server
    //ms hd Query li drna db atjib lina mn BookTransactionHistory l owner id dl book o rbtha lia b var userId li ankhdmo biha ltht o f server
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Integer userId);


    //hna bch nshofo all books borrowed (ktoba li deja mkriyin)
    @Query("""
            SELECT 
            (COUNT(*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory 
            WHERE bookTransactionHistory.user.id = :userId
            AND bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returned = false
            AND bookTransactionHistory.returnApproved = false
            """)    //(lakan returnApproved = false y3ni deja lktab borrowed by user whd akher)
    //hd Query ktgolih select mn BookTransactionHistory lifiha user.id o book.id nfs l9iyam li an3tiw ltht o lifiha returnApproved = false jib lia mn hdshi kml 3an tari9 COUNT gae isBorrowed
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);


    //hna anjibo l user id o book id li endhom nfs l 9iyam li an3tiw ltht o li endhom l book deja mekririn (returned= false)
    @Query("""
            SELECT transaction 
            FROM BookTransactionHistory transaction
            WHERE transaction.user.id = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            ORDER BY transaction.createdDate DESC
            """)
    List<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer userId); //Optional ktfrd elik dir dik orelse throw ra drnaha f service hit tshekina tma wsh user deja borrowa l book bch y9d ydir lih returned


    //nfs blan dl fo9 a hna anjobo l owner o ykon deja returned = true
    @Query("""
            SELECT transaction 
            FROM BookTransactionHistory transaction
            WHERE transaction.book.owner.id = :id
            AND transaction.book.id = :bookId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            ORDER BY transaction.createdDate DESC
            """)

    List<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer id);
}
