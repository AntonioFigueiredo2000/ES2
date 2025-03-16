package src;

import src.Password.PasswordManager;

public class Main {
    public static void main(String[] args) {
        PasswordManager manager = new PasswordManager();
        manager.generateAndStore("António", ConfiguracaoCentral.getInstancia() );
        manager.generateAndStore("João", ConfiguracaoCentral.getInstancia() );
        System.out.println(manager.getPasswordByUser("António"));
        manager.removePasswordByUser("João");
        System.out.println(manager.getPasswordByUser("João"));
    }
}