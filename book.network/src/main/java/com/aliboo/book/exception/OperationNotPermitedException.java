package com.aliboo.book.exception;

public class OperationNotPermitedException extends RuntimeException{ //bghitha tkon non echecked exeption y3ni tkhdm hd l exeption ir f runtime

    public OperationNotPermitedException(String msg){
        super(msg); //hd var msg 3mrnaha fl bookservice o drna super bch t9d takhd dik 9ima dyalna mn bookservice
    }
}
