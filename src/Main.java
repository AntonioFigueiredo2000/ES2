package src;

import src.Password.FileStorage;
import src.Password.PasswordManager;
import src.Password.PasswordMemento;
import src.autenticacao.AlertaSeguranca;
import src.autenticacao.Autenticacao;
import src.autenticacao.AutenticacaoMultifator;
import src.autenticacao.AutenticacaoSimples;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Cria o PasswordManager com FileStorage
        PasswordManager manager = new PasswordManager(new FileStorage());

        // Gera e adiciona passwords em categorias
        manager.generateAndStore("António", "pessoal", ConfiguracaoCentral.getInstancia());
        manager.generateAndStore("João", "pessoal/produtividade/Trello", ConfiguracaoCentral.getInstancia());
        manager.generateAndStore("Maria", "pessoal/Telefone", ConfiguracaoCentral.getInstancia());

        // Mostra a estrutura
        System.out.println("Estrutura de categorias e passwords (encriptadas):");
        manager.getRoot().display(0);

        // Salva o estado após gerar senhas
        PasswordMemento memento = manager.saveState();
        System.out.println("\nEstado salvo: Categoria=" + memento.getLastCategory() + ", Senha=" + memento.getLastPassword());

        // Recupera uma senha para mudar o estado
        String decryptedPassword = manager.getPasswordByUser("pessoal/Telefone/Maria");
        System.out.println("\nApós recuperação:");
        System.out.println("Senha recuperada: " + decryptedPassword); // Usa a variável aqui
        System.out.println("Categoria atual: " + manager.getLastCategory());
        System.out.println("Senha atual: " + manager.getLastPassword());

        // Restaura o estado anterior
        manager.restoreState(memento);
        System.out.println("\nApós restauração:");
        System.out.println("Categoria restaurada: " + manager.getLastCategory());
        System.out.println("Senha restaurada: " + manager.getLastPassword());



        System.out.println("\n------------------------Autenticacao multifator-----------------------------:");

        Autenticacao autenticacaoSimples = new AutenticacaoSimples();
        Autenticacao autenticacaoComMultifator = new AutenticacaoMultifator(autenticacaoSimples);
        Autenticacao autenticacaoComAlerta = new AlertaSeguranca(autenticacaoComMultifator);

        // Testa a autenticação
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o nome de utilizador: ");
        String utilizador = scanner.nextLine();
        System.out.print("Digite a password: ");
        String password = scanner.nextLine();

        boolean autenticado = autenticacaoComAlerta.autenticar(utilizador, password);

        if (autenticado) {
            System.out.println("Usuário autenticado com sucesso!");
        } else {
            System.out.println("Falha na autenticação.");
        }
    }
}


