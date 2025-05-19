package src.autenticacao;

public class AlertaSeguranca extends AutenticacaoDecorator {

    public AlertaSeguranca(Autenticacao autenticacao) {
        super(autenticacao);
    }

    @Override
    public boolean autenticar(String utilizador, String password) {
        boolean autenticado = super.autenticar(utilizador, password);

        if (autenticado) {
            System.out.println("Alerta: login efetuado com sucesso para " + utilizador);
        } else {
            System.out.println("Alerta: tentativa de login falhou para " + utilizador);
        }
        return autenticado;
    }
}