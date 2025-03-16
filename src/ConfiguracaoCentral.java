package src;

import java.util.Properties;

/**
 * Classe singleton para armazenar configurações centrais da aplicação.
 */
public class ConfiguracaoCentral {
    // Instância única da classe (Singleton)
    private static ConfiguracaoCentral instancia;

    // Objeto Properties para armazenar as configurações
    private Properties configuracoes;

    /**
     * Construtor privado para impedir a instanciação externa.
     * Define configurações padrão da aplicação.
     */
    private ConfiguracaoCentral() {
        this.configuracoes = new Properties();
        configuracoes.setProperty("metodo_encriptacao", String.valueOf(Enums.EncryptMethod.AES));
        configuracoes.setProperty("armazenamento", String.valueOf(Enums.StorageType.LOCAL));
        configuracoes.setProperty("politica_senha", String.valueOf(Enums.Criteria.STRONG));
    }

    /**
     * Retorna a instância única da classe.
     * @return Instância de ConfiguracaoCentral
     */
    public static ConfiguracaoCentral getInstancia() {
        if(instancia == null) {
            instancia = new ConfiguracaoCentral();
        }
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
