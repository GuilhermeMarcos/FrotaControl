package br.com.frotacontrol.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexões com o banco de dados MySQL.
 * O endereço, o usuário e a senha ficam no arquivo banco.properties
 * (pasta src/main/resources), para não precisar alterar o código.
 */
public final class Conexao {

    private static final String ARQUIVO = "/banco.properties";
    private static final Properties CONFIGURACAO = carregarConfiguracao();

    private Conexao() {
    }

    private static Properties carregarConfiguracao() {
        Properties propriedades = new Properties();
        try (InputStream entrada = Conexao.class.getResourceAsStream(ARQUIVO)) {
            if (entrada != null) {
                propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            }
        } catch (IOException erro) {
            // Sem o arquivo, valem os valores padrão indicados em abrir()
        }
        return propriedades;
    }

    /**
     * Abre uma conexão nova. Quem chama deve fechá-la (try-with-resources).
     */
    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(getUrl(),
                CONFIGURACAO.getProperty("banco.usuario", "root"),
                CONFIGURACAO.getProperty("banco.senha", ""));
    }

    public static String getUrl() {
        return CONFIGURACAO.getProperty("banco.url", "jdbc:mysql://localhost:3306/frotacontrol");
    }

    /**
     * Confere, ao iniciar o sistema, se o banco está acessível e se as tabelas existem.
     */
    public static void testar() {
        try (Connection conexao = abrir();
                java.sql.Statement comando = conexao.createStatement()) {
            comando.executeQuery("SELECT COUNT(*) FROM usuario").close();
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível conectar ao banco de dados.", erro);
        }
    }
}
