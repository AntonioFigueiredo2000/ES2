public class Main {
    public static void main(String[] args) {
        ConfiguracaoCentral config = ConfiguracaoCentral.getInstancia();

        // Exibir configuração inicial
        System.out.println("Método de Encriptação: " + config.get("metodo_encriptacao"));
        System.out.println("Armazenamento: " + config.get("armazenamento"));
        System.out.println("Política de Senha: " + config.get("politica_senha"));

        // Modificar uma configuração
        config.set("armazenamento", "cloud");

        // Exibir configuração após alteração
        System.out.println("\nApós alteração:");
        System.out.println("Armazenamento: " + config.get("armazenamento"));
    }
}
