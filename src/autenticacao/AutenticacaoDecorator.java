package src.autenticacao;

public abstract class AutenticacaoDecorator implements Autenticacao {
    protected Autenticacao autenticacao;

    public AutenticacaoDecorator(Autenticacao autenticacao) {
        this.autenticacao = autenticacao;
    }

    public boolean autenticar(String utilizador, String password) {
        return this.autenticacao.autenticar(utilizador, password);
    }
}
