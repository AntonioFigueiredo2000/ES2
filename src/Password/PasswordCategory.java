package src.Password;

import java.util.ArrayList;
import java.util.List;

public class PasswordCategory implements PasswordComponent {
    private String name;
    private List<PasswordComponent> components;

    public PasswordCategory(String name) {
        this.name = name;
        this.components = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void add(PasswordComponent component) {
        components.add(component);
    }

    @Override
    public void remove(PasswordComponent component) {
        components.remove(component);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void display(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("-");
        }
        System.out.println(indent + name);
        for (PasswordComponent component : components) {
            component.display(depth + 2);
        }
    }

    // Novo método para expor os filhos
    public List<PasswordComponent> getChildren() {
        return components;
    }
}