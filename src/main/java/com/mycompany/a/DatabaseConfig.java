package com.mycompany.a;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitário de configuração de acesso ao banco de dados.
 *
 * <p>Resolve as credenciais a partir de duas fontes, nesta ordem:
 * <ol>
 *   <li>Arquivo {@code db.properties} no classpath (em {@code src/main/resources/}).</li>
 *   <li>Variáveis de ambiente {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}.</li>
 * </ol>
 *
 * <p>Se nenhuma das fontes fornecer todas as três chaves, é lançada
 * {@link IllegalStateException} no carregamento da classe.
 */
public final class DatabaseConfig {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        String url = null;
        String user = null;
        String password = null;

        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao ler db.properties do classpath.", e);
        }

        if (url == null || url.isBlank()) {
            url = System.getenv("DB_URL");
        }
        if (user == null || user.isBlank()) {
            user = System.getenv("DB_USER");
        }
        if (password == null || password.isBlank()) {
            password = System.getenv("DB_PASSWORD");
        }

        if (url == null || url.isBlank()
                || user == null || user.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Configuração de banco ausente. Defina db.url/db.user/db.password "
                  + "em src/main/resources/db.properties (use db.properties.example como base) "
                  + "ou exporte as variáveis de ambiente DB_URL, DB_USER e DB_PASSWORD.");
        }

        URL = url;
        USER = user;
        PASSWORD = password;
    }

    private DatabaseConfig() {
        // utilitário, não instanciar
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
