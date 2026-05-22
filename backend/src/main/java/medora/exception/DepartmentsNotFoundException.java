package medora.exception;

public class DepartmentsNotFoundException extends RuntimeException {
    public DepartmentsNotFoundException(Long id) {
        super("A department with id %d does not exist.".formatted(id));
    }
}

