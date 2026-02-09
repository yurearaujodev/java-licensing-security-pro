package com.br.yat.gerenciador.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.configurations.DatabaseConfigLoader;
import com.br.yat.gerenciador.exception.CryptoException;
import com.br.yat.gerenciador.model.dto.DatabaseConfigDTO;
import com.br.yat.gerenciador.security.AESUtils;
import com.br.yat.gerenciador.security.FileManager;
import com.br.yat.gerenciador.security.KeyManager;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.DatabaseValidationUtils;

public class DatabaseSetupService {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupService.class);

	private static final Path CONFIG_DIR = Paths.get("config", "database");
	private static final Path DB_CONFIG_FILE = CONFIG_DIR.resolve("db.properties");
	private static final Path GLOBAL_MASTER_KEY_FILE = CONFIG_DIR.resolve("master.key");

	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	public void saveDatabaseConfigConfiguration(DatabaseConfigDTO dto) {
		DatabaseValidationUtils.validarConfiguracaoCompleta(dto.url(), dto.user(), dto.password());
		
		byte[] encryptedPassword = null;
		byte[] encryptedUser = null;
		byte[] encryptedUrl = null;

		try {
			ensureConfigDirectory();

			SecretKey masterKey = loadOrCreateMasterKey();
			
			encryptedPassword = AESUtils.encrypt(dto.password(), masterKey);
			encryptedUrl = AESUtils.encrypt(dto.url(), masterKey);
			encryptedUser = AESUtils.encrypt(dto.user(), masterKey);
			
			String encodedPassword = Base64.getEncoder().encodeToString(encryptedPassword);
			String encodedUrl = Base64.getEncoder().encodeToString(encryptedUrl);
			String encodedUser = Base64.getEncoder().encodeToString(encryptedUser);

			Properties props = buildProperties(encodedUrl, encodedUser, encodedPassword);

			DatabaseConfigLoader.validateRequiredProperties(props);

			FileManager.saveText(DB_CONFIG_FILE, toPropertiesString(props));
			logger.info("Configuração de banco salva com sucesso.");

		} catch (Exception e) {
			logger.error("ERRO CRÍTICO AO SALVAR CONFIGURAÇÃO: {}", e.getMessage());
            throw new CryptoException("ERRO TÉCNICO AO GRAVAR CONFIGURAÇÕES", e);
		} finally {
			SensitiveData.safeClear(encryptedPassword);
			SensitiveData.safeClear(encryptedUrl);
			SensitiveData.safeClear(encryptedUser);
			SensitiveData.safeClear(dto.password());
		}
	}
	
	public DatabaseConfigDTO carregarConfiguracao() {
	    if (!Files.exists(DB_CONFIG_FILE)) {
	        return null;
	    }

	    try {
	        Properties props = new Properties();
	        props.load(Files.newBufferedReader(DB_CONFIG_FILE));

	        SecretKey masterKey = loadOrCreateMasterKey();

	        String rawUrl = props.getProperty("db.url");
	        String rawUser = props.getProperty("db.user");

	        String url = ValidationUtils.isBase64(rawUrl)
	            ? AESUtils.decryptToString(Base64.getDecoder().decode(rawUrl), masterKey)
	            : rawUrl;

	        String user = ValidationUtils.isBase64(rawUser)
	            ? AESUtils.decryptToString(Base64.getDecoder().decode(rawUser), masterKey)
	            : rawUser;
	        return new DatabaseConfigDTO(url, user, null);

	    } catch (Exception e) {
	        logger.error("Falha ao carregar configuração do banco", e);
	        throw new CryptoException("Erro técnico ao ler configurações", e);
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

	private static Properties buildProperties(String encodedUrl, String encodedUser, String encodedPassword) {
		Properties props = new Properties();

		props.setProperty("db.url", encodedUrl.trim());
		props.setProperty("db.user", encodedUser.trim());
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
		sb.append("# GERADO AUTOMATICAMENTE NO PRIMEIRO ACESSO\n");
		props.stringPropertyNames().stream().sorted()
				.forEach(key -> sb.append(key).append('=').append(props.getProperty(key)).append('\n'));
		return sb.toString();
	}
}
