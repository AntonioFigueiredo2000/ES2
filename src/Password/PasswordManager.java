package src.Password;

import src.ConfiguracaoCentral;

public class PasswordManager {
    private IStorage storage;
    private PasswordComponent root;

    public PasswordManager(IStorage storage) {
        this.storage = storage;
        this.root = ((FileStorage) storage).load();
    }

    public void generateAndStore(String username, String categoryPath, ConfiguracaoCentral config) {
        IPasswordGenerator generator = PasswordGeneratorFactory.create(config.get("politica_senha"));
        String password = generator.generate();

        // Encontra a categoria alvo
        PasswordComponent targetCategory = findOrCreateCategory(categoryPath);

        // Verifica se o utilizador já existe na categoria
        if (targetCategory instanceof PasswordCategory) {
            for (PasswordComponent child : ((PasswordCategory) targetCategory).getChildren()) {
                if (child.getName().equals(username)) {
                    // Se o utilizador já existe, remove-o
                    targetCategory.remove(child);
                    break;
                }
            }
        }

        // Adiciona a nova password
        PasswordComponent leaf = new PasswordLeaf(username, password);
        targetCategory.add(leaf);

        // Salva a hierarquia
        storage.save(root);

        System.out.println("Foi gerada a password do utilizador " + username + " na categoria " + categoryPath);
    }

    public String getPasswordByUser(String username) {
        return storage.get(username);
    }

    public void removePasswordByUser(String username) {
        storage.remove(username);
    }

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