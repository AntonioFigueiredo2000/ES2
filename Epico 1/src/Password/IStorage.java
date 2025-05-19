package src.Password;

public interface IStorage {
    void save(PasswordComponent root); // Salva a hierarquia completa
    String get(String username); // Mantém para compatibilidade
    void remove(String username); // Mantém para compatibilidade
}