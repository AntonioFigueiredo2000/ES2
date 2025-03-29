package src.Password;

public class PasswordMemento {
    private final String lastCategory; // Última categoria acessada
    private final String lastPassword; // Última senha consultada ou gerada

    public PasswordMemento(String lastCategory, String lastPassword) {
        this.lastCategory = lastCategory;
        this.lastPassword = lastPassword;
    }

    public String getLastCategory() {
        return lastCategory;
    }

    public String getLastPassword() {
        return lastPassword;
    }
}