package src.Password;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileStorage implements IStorage {
    private String filePath = "passwords.txt";
    private Map<String, String> passwordMap; // Cache para acesso rápido

    public FileStorage() {
        passwordMap = new HashMap<>();
    }

    // Novo método para carregar a hierarquia do ficheiro
    public PasswordComponent load() {
        PasswordComponent root = new PasswordCategory("root");
        Map<String, PasswordComponent> categoryMap = new HashMap<>();
        categoryMap.put("root", root);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String type = parts[0]; // "C" para categoria, "P" para password
                String path = parts[1]; // Caminho (ex.: "root/pessoal/bancos/João")

                if (type.equals("C")) {
                    // Cria a categoria se ainda não existir
                    if (!categoryMap.containsKey(path)) {
                        String[] pathParts = path.split("/");
                        String parentPath = String.join("/", java.util.Arrays.copyOf(pathParts, pathParts.length - 1));
                        PasswordComponent parent = categoryMap.getOrDefault(parentPath, root);
                        PasswordComponent category = new PasswordCategory(pathParts[pathParts.length - 1]);
                        parent.add(category);
                        categoryMap.put(path, category);
                    }
                } else if (type.equals("P") && parts.length == 3) {
                    String password = parts[2];
                    String[] pathParts = path.split("/");
                    String username = pathParts[pathParts.length - 1];
                    String parentPath = String.join("/", java.util.Arrays.copyOf(pathParts, pathParts.length - 1));
                    PasswordComponent parent = categoryMap.getOrDefault(parentPath, root);
                    PasswordComponent leaf = new PasswordLeaf(username, password);
                    parent.add(leaf);
                    passwordMap.put(path, password); // Atualiza o cache
                }
            }
        } catch (IOException e) {
            System.err.println("Ficheiro não encontrado, começando com hierarquia vazia.");
        }

        return root;
    }

    @Override
    public void save(PasswordComponent root) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            saveComponent(root, "", writer);
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
            passwordMap.put(currentPath, component.getPassword());
        }
    }

    @Override
    public String get(String username) {
        return passwordMap.get(username);
    }

    @Override
    public void remove(String username) {
        passwordMap.remove(username);
        // Nota: Precisaremos recarregar a hierarquia para refletir a remoção
    }
}