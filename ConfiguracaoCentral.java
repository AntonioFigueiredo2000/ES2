import java.util.Properties;

/**
 * Classe singleton para armazenar configurações centrais da aplicação.
 */
public class ConfiguracaoCentral {
    // Instância única da classe (Singleton)
    private static final ConfiguracaoCentral instancia = new ConfiguracaoCentral();

    // Objeto Properties para armazenar as configurações
    private final Properties configuracoes = new Properties();

    /**
     * Construtor privado para impedir a instanciação externa.
     * Define configurações padrão da aplicação.
     */
    private ConfiguracaoCentral() {
        configuracoes.setProperty("metodo_encriptacao", "AES");
        configuracoes.setProperty("armazenamento", "local");
        configuracoes.setProperty("politica_senha", "forte");
    }

    /**
     * Retorna a instância única da classe.
     * @return Instância de ConfiguracaoCentral
     */
    public static ConfiguracaoCentral getInstancia() {
        return instancia;
    }

    /**
     * Obtém o valor de uma configuração específica.
     * @param chave Nome da configuração
     * @return Valor associado à chave, ou null se não existir
     */
    public String get(String chave) {
        return configuracoes.getProperty(chave);
    }

    /**
     * Define ou altera uma configuração específica.
     * @param chave Nome da configuração
     * @param valor Novo valor a ser atribuído
     */
    public void set(String chave, String valor) {
        configuracoes.setProperty(chave, valor);
    }
}
