package src.Password;

public interface PasswordComponent {
    String getName(); // Nome da categoria ou utilizador
    void add(PasswordComponent component); // Adicionar subcategoria ou password
    void remove(PasswordComponent component); // Remover subcategoria ou password
    String getPassword(); // Obter a password (null para categorias)
    void display(int depth); // Mostrar a estrutura (para navegação)
}