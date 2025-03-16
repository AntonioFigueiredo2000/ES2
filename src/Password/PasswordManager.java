package src.Password;

import src.ConfiguracaoCentral;

import java.util.HashMap;
import java.util.Map;


public class PasswordManager {
    private Map<String, String> passwords = new HashMap<>();

    public void generateAndStore(String username, ConfiguracaoCentral config){
        IPasswordGenerator generator = PasswordGeneratorFactory.create(config.get("politica_senha"));
        String password = generator.generate();
        passwords.put(username,password);
        System.out.println("Foi gerada a password do utilizador " + username);
    }

    public String getPasswordByUser(String username) {
        return passwords.get(username);
    }

    public void removePasswordByUser(String username) {
        this.passwords.remove(username);
    }
}
