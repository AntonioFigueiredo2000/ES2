package src.autenticacao;

import java.util.Scanner;

public class AutenticacaoMultifator extends AutenticacaoDecorator {

    public AutenticacaoMultifator(Autenticacao autenticacao) {
        super(autenticacao);
    }

    public boolean autenticar(String utilizador, String password) {
        boolean autenticado = super.autenticar(utilizador, password);
        if (autenticado) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Digite o código de autenticação multifator: ");
            String codigo = scanner.nextLine();
            if ("123456".equals(codigo)) {
                System.out.println("Autenticação multifator bem-sucedida.");
                return true;
            } else {
                System.out.println("Falha na autenticação multifator.");
                return false;
            }
        } else {
            System.out.println("Falha na autenticação básica. Multifator não solicitado.");
            return false;
        }
    }
}
