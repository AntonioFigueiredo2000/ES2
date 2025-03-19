package src;

import src.Password.FileStorage;
import src.Password.PasswordManager;

public class Main {
    public static void main(String[] args) {
        // Cria o PasswordManager com FileStorage
        PasswordManager manager = new PasswordManager(new FileStorage());

        // Gera e adiciona passwords em categorias
        manager.generateAndStore("António", "pessoal", ConfiguracaoCentral.getInstancia());
        manager.generateAndStore("João", "pessoal/produtividade/Trello", ConfiguracaoCentral.getInstancia());
        manager.generateAndStore("Maria", "pessoal/Telefone", ConfiguracaoCentral.getInstancia());

        // Mostra a estrutura
        System.out.println("Estrutura de categorias e passwords:");
        manager.getRoot().display(0);

    }
}