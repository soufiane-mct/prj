package com.aliboo.book.common;

//hna endna vars kit3awdo fl feedback o l book id create date lastModifiedBy .. ola bghina n3dlo o la n3mrohom khas nbdlohom bjoj kl whda bohdha dkshi elsh ankhdmo b inheritance (lihia l wirata )
//o andiro hd vars li ltht an3tiwhom l book o l feedback ayritohom mn hd baseEntity
//o mn b3d lbghina nzido shi filde knzidoh ahna kimshi lihom bjj ola t9d tkhdm bih f bzff d classes ..

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder //hna fsh tikon endk inheritance bin java classes o bghiti tkhdm b builder ma atkhdmsh khas tkhdm b superBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass //fsh katkon khdam b inheritance o ktbghi dik mapping
@EntityListeners(AuditingEntityListener.class)//hdi li at3wna f @CreatedBy o LastModifiedBy hit spring ma ay3rfsh yjib l user o ykhdm bif f hado ms f securityconfig f lwl khlinah ykhdm bih

public class BaseEntity {
    @Id
    @GeneratedValue
    private  Integer id;
    @CreatedDate
    @Column(nullable = false,updatable = false) //madirsh lo update o maykonsh khawi
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)//mdirsh liha insert (mt3dlsh eliha)
    private LocalDateTime lasteModifiedDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private  Integer cratedBy;

    @LastModifiedBy //hdi bch traki lina li cratedBy o ye3tina lastModifiedBy
    @Column(insertable = false)
    private Integer lastModifiedBy;
}
