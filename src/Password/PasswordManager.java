package src.Password;

import src.ConfiguracaoCentral;

public class PasswordManager {
    private IStorage storage;
    private PasswordComponent root;
    private EncryptionPool encryptionPool;
    private String lastCategory;
    private String lastPassword;

    public PasswordManager(IStorage storage) {
        this.storage = storage;
        this.root = ((FileStorage) storage).load();
        this.encryptionPool = EncryptionPool.getInstance();
        this.lastCategory = null;
        this.lastPassword = null;
    }

    // Interface interna para acesso aos dados do Memento, visível apenas dentro de PasswordManager
    private interface MementoAccess {
        String getLastCategory();
        String getLastPassword();
        PasswordComponent getRootSnapshot();
    }

    // Classe interna pública para o Memento
    public static class PasswordMemento implements MementoAccess {
        private final String lastCategory;
        private final String lastPassword;
        private final PasswordComponent rootSnapshot;

        private PasswordMemento(String lastCategory, String lastPassword, PasswordComponent rootSnapshot) {
            this.lastCategory = lastCategory;
            this.lastPassword = lastPassword;
            this.rootSnapshot = rootSnapshot;
        }

        @Override
        public String getLastCategory() {
            return lastCategory;
        }

        @Override
        public String getLastPassword() {
            return lastPassword;
        }

        @Override
        public PasswordComponent getRootSnapshot() {
            return rootSnapshot;
        }
    }

    public PasswordMemento saveState() {
        return new PasswordMemento(lastCategory, lastPassword, deepCopy(root));
    }

    public void restoreState(PasswordMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Memento inválido");
        }
        if (isValidCategory(memento.getLastCategory())) {
            this.lastCategory = memento.getLastCategory();
            this.lastPassword = memento.getLastPassword();
            this.root = deepCopy(memento.getRootSnapshot());
            storage.save(root); // Persistir a hierarquia restaurada
        } else {
            throw new IllegalStateException("Categoria inválida para restauração: " + memento.getLastCategory());
        }
    }

    private boolean isValidCategory(String category) {
        if (category == null || category.isEmpty()) return true; // Permite null para estado inicial
        String[] categories = category.split("/");
        PasswordComponent current = root;
        for (String cat : categories) {
            if (cat.isEmpty()) continue;
            boolean found = false;
            if (current instanceof PasswordCategory) {
                for (PasswordComponent child : ((PasswordCategory) current).getChildren()) {
                    if (child.getName().equals(cat)) {
                        current = child;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private PasswordComponent deepCopy(PasswordComponent component) {
        if (component == null) return null;
        if (component instanceof PasswordLeaf) {
            PasswordLeaf leaf = (PasswordLeaf) component;
            return new PasswordLeaf(leaf.getName(), leaf.getPassword());
        } else if (component instanceof PasswordCategory) {
            PasswordCategory category = (PasswordCategory) component;
            PasswordCategory newCategory = new PasswordCategory(category.getName());
            for (PasswordComponent child : category.getChildren()) {
                newCategory.add(deepCopy(child));
            }
            return newCategory;
        }
        return null;
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

        this.lastCategory = categoryPath;
        this.lastPassword = password;
        System.out.println("Foi gerada a password do utilizador " + username + " na categoria " + categoryPath);
    }

    public String getPasswordByUser(String username) {
        String encryptedPassword = storage.get(username);
        if (encryptedPassword != null) {
            IEncryptor encryptor = encryptionPool.acquireEncryptor();
            String decryptedPassword = encryptor.decrypt(encryptedPassword);
            encryptionPool.releaseEncryptor(encryptor);

            this.lastCategory = username.substring(0, username.lastIndexOf("/"));
            this.lastPassword = decryptedPassword;
            return decryptedPassword;
        }
        return null;
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

    public String getLastCategory() {
        return lastCategory;
    }

    public String getLastPassword() {
        return lastPassword;
    }
}