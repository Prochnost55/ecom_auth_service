package procho.dev.ecomm.user.authentication.exceptions;


// RuntimeException is the superclass of those exceptions that can be thrown during the normal operation of the Java Virtual Machine.
// RuntimeException and its subclasses are unchecked exceptions.
// Unchecked exceptions do not need to be declared in a method or constructor's throws clause if they can be thrown by the execution of the method or constructor and propagate outside the method or constructor boundary.
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(){};
    public UserNotFoundException(String message){
        super(message);
    }
    public UserNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
