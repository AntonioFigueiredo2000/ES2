package src.Password;

public class PasswordLeaf implements PasswordComponent {
    private String username; // Nome do utilizador (ex.: "António")
    private String password; // Password associada (ex.: "A!s9@Lm#2P")

    // Construtor
    public PasswordLeaf(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public void add(PasswordComponent component) {
        // Folhas não podem adicionar outros componentes
        throw new UnsupportedOperationException("Cannot add to a leaf");
    }

    @Override
    public void remove(PasswordComponent component) {
        // Folhas não podem remover outros componentes
        throw new UnsupportedOperationException("Cannot remove from a leaf");
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void display(int depth) {
        // Mostra o nome do utilizador e a password com indentação
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("-");
        }
        System.out.println(indent + username + ": " + password);
    }
}