package com.aliboo.book.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account") //hdi hit enum o l templaite naming o ACTIVATE_ACCOUNTdrna file d html f templates
    ;
    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
