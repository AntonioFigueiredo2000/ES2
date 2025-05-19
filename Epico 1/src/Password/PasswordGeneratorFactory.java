package src.Password;

import src.Enums;

// Factory class that decides which implementation to use based on criteria
public class PasswordGeneratorFactory {
    public static IPasswordGenerator create(String criteria) {
        if (String.valueOf(Enums.Criteria.STRONG).equalsIgnoreCase(criteria)) {
            return new StrongPasswordGenerator();
        } else {
            return new WeakPasswordGenerator();
        }
    }
}