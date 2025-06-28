package com.aliboo.book.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    //hd spec antib9oha fl book Specification<Book>
    public static Specification<Book> withOwnerId(Integer ownerId) { //hna anjibo books mohadadin li endhom nfs l owner id 3an tari9 Specification d spring
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id") , ownerId); //hna katgolih dkhl l book o jib mno l owner id o an3tiwha l ownerId lihia var li drna fo9
    }
}
