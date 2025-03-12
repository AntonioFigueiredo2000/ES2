package src.Password;

// Concrete implementation for generating a weak password
public class WeakPasswordGenerator implements IPasswordGenerator {
    @Override
    public String generate() {
        // Logic for generating a weak password
        return "123456";
    }
}