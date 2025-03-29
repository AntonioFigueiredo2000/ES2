package src.Password;

import src.ConfiguracaoCentral;

public class PasswordManager {
    private IStorage storage;
    private PasswordComponent root;
    private EncryptionPool encryptionPool;
    private String lastCategory;  // Última categoria acessada
    private String lastPassword;  // Última senha consultada ou gerada

    public PasswordManager(IStorage storage) {
        this.storage = storage;
        this.root = ((FileStorage) storage).load();
        this.encryptionPool = EncryptionPool.getInstance();
        this.lastCategory = null;
        this.lastPassword = null;
    }

    public void generateAndStore(String username, String categoryPath, ConfiguracaoCentral config) {
        IPasswordGenerator generator = PasswordGeneratorFactory.create(config.get("politica_senha"));
        String password = generator.generate();

        IEncryptor encryptor = encryptionPool.acquireEncryptor();
        String encryptedPassword = encryptor.encrypt(password);

        PasswordComponent targetCategory = findOrCreateCategory(categoryPath);

        if (targetCategory instanceof PasswordCategory) {
            for (PasswordComponent child : ((PasswordCategory) targetCategory).getChildren()) {
                if (child.getName().equals(username)) {
                    targetCategory.remove(child);
                    break;
                }
            }
        }

        PasswordComponent leaf = new PasswordLeaf(username, encryptedPassword);
        targetCategory.add(leaf);

        storage.save(root);
        encryptionPool.releaseEncryptor(encryptor);

        // Atualiza o estado
        this.lastCategory = categoryPath;
        this.lastPassword = password; // Salva desencriptada

        System.out.println("Foi gerada a password do utilizador " + username + " na categoria " + categoryPath);
    }

    public String getPasswordByUser(String username) {
        String encryptedPassword = storage.get(username);
        if (encryptedPassword != null) {
            IEncryptor encryptor = encryptionPool.acquireEncryptor();
            String decryptedPassword = encryptor.decrypt(encryptedPassword);
            encryptionPool.releaseEncryptor(encryptor);

            // Atualiza o estado
            this.lastCategory = username.substring(0, username.lastIndexOf("/"));
            this.lastPassword = decryptedPassword;

            return decryptedPassword;
        }
        return null;
    }

    // Métodos do Memento
    public PasswordMemento saveState() {
        return new PasswordMemento(lastCategory, lastPassword);
    }

    public void restoreState(PasswordMemento memento) {
        this.lastCategory = memento.getLastCategory();
        this.lastPassword = memento.getLastPassword();
    }

    public String getLastCategory() {
        return lastCategory;
    }

    public String getLastPassword() {
        return lastPassword;
    }

    // Métodos restantes sem alteração: removePasswordByUser, findOrCreateCategory, getRoot
    private PasswordComponent findOrCreateCategory(String categoryPath) {
        String[] categories = categoryPath.split("/");
        PasswordComponent current = root;

        for (String category : categories) {
            if (category.isEmpty()) continue;
            boolean found = false;
            if (current instanceof PasswordCategory) {
                for (PasswordComponent child : ((PasswordCategory) current).getChildren()) {
                    if (child.getName().equals(category)) {
                        current = child;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                PasswordComponent newCategory = new PasswordCategory(category);
                current.add(newCategory);
                current = newCategory;
            }
        }
        return current;
    }

    public PasswordComponent getRoot() {
        return root;
    }
}