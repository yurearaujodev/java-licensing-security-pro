package com.br.yat.gerenciador.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.config.DatabaseConfigLoader;
import com.br.yat.gerenciador.exception.CryptoException;
import com.br.yat.gerenciador.security.AESUtils;
import com.br.yat.gerenciador.security.FileManager;
import com.br.yat.gerenciador.security.KeyManager;
import com.br.yat.gerenciador.security.SensitiveData;

public final class DatabaseSetupService {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupService.class);

	private static final Path CONFIG_DIR = Paths.get("config", "database");
	private static final Path DB_CONFIG_FILE = CONFIG_DIR.resolve("db.properties");
	private static final Path GLOBAL_MASTER_KEY_FILE = CONFIG_DIR.resolve("master.key");

	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	private DatabaseSetupService() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static void saveDatabaseConfigConfiguration(String url, String user, char[] password) {
		Objects.requireNonNull(url, "db.url não pode ser nulo");
		Objects.requireNonNull(user, "db.user não pode ser nulo");
		Objects.requireNonNull(password, "db.password não pode ser nulo");

		byte[] encrytedPassword = null;

		try {
			ensureConfigDirectory();

			SecretKey masterKey = loadOrCreateMasterKey();
			encrytedPassword = AESUtils.encrypt(password, masterKey);
			String encodedPassword = Base64.getEncoder().encodeToString(encrytedPassword);

			Properties props = buildProperties(url, user, encodedPassword);

			DatabaseConfigLoader.validateRequiredProperties(props);

			FileManager.saveText(DB_CONFIG_FILE, toPropertiesString(props));
			logger.info("Configuração de banco salva com sucesso em {}", DB_CONFIG_FILE);

		} catch (CryptoException e) {
			throw e;

		} catch (Exception e) {
			logger.error("Falha ao salvar configuração do banco", e);
			throw new CryptoException("Erro ao salvar configuração do banco", e);
		} finally {
			SensitiveData.safeClear(encrytedPassword);
			SensitiveData.safeClear(password);
		}

	}

	private static void ensureConfigDirectory() {
		try {
			Files.createDirectories(CONFIG_DIR);
		} catch (Exception e) {
			throw new CryptoException("Falha ao criar diretorório de configuração do banco", e);
		}
	}

	private static SecretKey loadOrCreateMasterKey() {
		if (Files.exists(GLOBAL_MASTER_KEY_FILE)) {
			logger.debug("Carregando master.key existente");
			return KeyManager.loadAES(GLOBAL_MASTER_KEY_FILE);
		}
		logger.info("Gerando nova master.key para banco de dados");
		return KeyManager.generateAndSaveAES(GLOBAL_MASTER_KEY_FILE);
	}

	private static Properties buildProperties(String url, String user, String encodedPassword) {
		Properties props = new Properties();

		props.setProperty("db.url", url.trim());
		props.setProperty("db.user", user.trim());
		props.setProperty("db.password", encodedPassword.trim());
		props.setProperty("db.driver", JDBC_DRIVER);

		applyDefaultProperties(props);
		return props;
	}

	private static void applyDefaultProperties(Properties props) {
		props.putIfAbsent("db.poolSize", "5");
		props.putIfAbsent("db.minIdle", "1");
		props.putIfAbsent("db.idleTimeout", "30000");
		props.putIfAbsent("db.maxLifeTime", "1200000");
		props.putIfAbsent("db.connectionTimeout", "300000");
		props.putIfAbsent("db.leakDetectionThreshold", "300000");
		props.putIfAbsent("db.validationTimeout", "5000");

		props.putIfAbsent("db.serverTimezone", "America/Sao_Paulo");
		props.putIfAbsent("db.characterEncoding", "UTF-8");
		props.putIfAbsent("db.useUnicode", "true");

		props.putIfAbsent("db.useSSL", "false");
		props.putIfAbsent("db.allowPublicKeyRetrieval", "true");
	}

	private static String toPropertiesString(Properties props) {
		StringBuilder sb = new StringBuilder();
		sb.append("# Configuração gerada na primeira execução\n");
		props.stringPropertyNames().stream().sorted()
				.forEach(key -> sb.append(key).append('=').append(props.getProperty(key)).append('\n'));
		return sb.toString();
	}
}
