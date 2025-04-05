package src.autenticacao;

public class AutenticacaoSimples implements Autenticacao {
    public AutenticacaoSimples() {
    }

    public boolean autenticar(String utilizador, String password) {
        System.out.println("Autenticando usuário básico...");
        return "Antonio".equals(utilizador) && "A!s9@Lm#2P".equals(password);
    }
}
