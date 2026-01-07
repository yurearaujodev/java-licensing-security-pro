package com.br.yat.gerenciador.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.Endereco;

/**
 * Classe utilitária para consulta de endereços via API pública do ViaCep.
 * <p>
 * Esta classe utiliza:
 * <ul>
 * <li><b>Java HTTP CLient</b> ({@code java.net.http}) para realizar requisições
 * assíncronas.</li>
 * <li><b>org.json</b> para parsear a resposta JSON.</li>
 * <li><b>SLF4J</b> para logging de erros e avisos.</li>
 * <ul>
 * 
 * <p>
 * Não deve ser instanciada.
 * </p>
 */
public final class CepUtils {

	private static final Logger logger = LoggerFactory.getLogger(CepUtils.class);

	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private CepUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Consulta o endereço correspondente a um CEP utilizando a API ViaCEP.
	 * 
	 * @param cep CEP informado (pode conter caracteres não numéricos)
	 * @return um{@link CompletableFuture} contendo {@link Optional} de {@link Endereco}
	 */
	public static CompletableFuture<Optional<Endereco>> searchCep(String cep) {
		String cleanCep = cleanCep(cep);
		if (cleanCep == null) {
			return CompletableFuture.completedFuture(Optional.empty());
		}
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://viacep.com.br/ws/" + cleanCep + "/json/"))
				.header("User-Agent", "GerenciadorLicencas/1.0")
				.timeout(Duration.ofSeconds(10))
				.GET()
				.build();

		return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(response -> {
			if (response.statusCode() != 200) {
				logger.warn("ViaCEP retornou status {} para CEP {}", response.statusCode(), cleanCep);
				return Optional.<Endereco>empty();
			}
		
			return parseEndereco(response.body());
			
		}).exceptionally(ex -> {
			logger.error("Erro ao consultar ViaCEP para CEP {}", cleanCep,ex);
			return Optional.empty();
		});
	}
	/**
	 * Converte a resposta JSON em um objeto {@link Endereco}
	 * 
	 * @param body resposta JSON da API ViaCEP
	 * @return {@link Optional} de {@link Endereco}, vazio se erro ou CEP inválido
	 */
	private static Optional<Endereco> parseEndereco(String body){
		try {
			JSONObject json = new JSONObject(body);
			if (json.optBoolean("erro",false)) {
				return Optional.empty();
			}
			Endereco endereco = new Endereco(
					json.getString("cep").replaceAll("\\D", ""), 
					json.optString("logradouro", ""),
					json.optString("complemento", ""),
					json.optString("bairro", ""), 
					json.optString("localidade", ""), 
					json.optString("estado", "")
					);
			
			return Optional.of(endereco);
		
		} catch (Exception e) {
			logger.error("Erro ao parsear resposta ViaCEP: {}", body, e);
			return Optional.empty();
		}
	}
	
	/**
	 *Limpa o CEP informado, removendo caracteres não numéricos.
	 * 
	 * @param cep CEP informado
	 * @return CEP apenas com dígitos, ou {@code null} se inválido
	 */
	private static String cleanCep(String cep) {
		if (cep==null|| cep.trim().isBlank()) {
			return null;
		}
		String clean = cep.replaceAll("\\D", "");
		return clean.matches("\\d{8}") ? clean : null;
	}

}
