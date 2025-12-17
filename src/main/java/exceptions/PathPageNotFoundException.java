package exceptions;

public class PathPageNotFoundException extends RuntimeException {

    public PathPageNotFoundException (Class clazz) {
        super(String.format("Path on page class %s mot found", clazz.getCanonicalName()));
    }
}
