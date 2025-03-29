package src.Password;

import java.io.*;

public class FileStorage implements IStorage {
    private String filePath = "passwords.txt";
    private PasswordComponent root;

    public FileStorage() {
        this.root = null;
    }

    public PasswordComponent load() {
        // Limpa o ficheiro ao iniciar para evitar duplicatas
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        root = new PasswordCategory("root");
        return root;
    }

    @Override
    public void save(PasswordComponent root) {
        this.root = root;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            if (root instanceof PasswordCategory) {
                for (PasswordComponent child : ((PasswordCategory) root).getChildren()) {
                    saveComponent(child, "", writer);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao guardar no ficheiro: " + e.getMessage());
        }
    }

    private void saveComponent(PasswordComponent component, String path, BufferedWriter writer) throws IOException {
        String currentPath = path.isEmpty() ? component.getName() : path + "/" + component.getName();

        if (component instanceof PasswordCategory) {
            writer.write("C:" + currentPath);
            writer.newLine();
            for (PasswordComponent child : ((PasswordCategory) component).getChildren()) {
                saveComponent(child, currentPath, writer);
            }
        } else if (component instanceof PasswordLeaf) {
            writer.write("P:" + currentPath + ":" + component.getPassword());
            writer.newLine();
        }
    }

    @Override
    public String get(String username) {
        if (root == null) return null;
        String[] pathParts = username.split("/");
        PasswordComponent current = root;
        for (int i = 0; i < pathParts.length - 1; i++) {
            if (current instanceof PasswordCategory) {
                boolean found = false;
                for (PasswordComponent child : ((PasswordCategory) current).getChildren()) {
                    if (child.getName().equals(pathParts[i])) {
                        current = child;
                        found = true;
                        break;
                    }
                }
                if (!found) return null;
            }
        }
        if (current instanceof PasswordCategory) {
            for (PasswordComponent child : ((PasswordCategory) current).getChildren()) {
                if (child.getName().equals(pathParts[pathParts.length - 1]) && child instanceof PasswordLeaf) {
                    return child.getPassword();
                }
            }
        }
        return null;
    }

    @Override
    public void remove(String username) {
        // TBD
    }
}