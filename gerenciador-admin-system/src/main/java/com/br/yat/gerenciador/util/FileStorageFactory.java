package com.br.yat.gerenciador.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FileStorageFactory {

	private static final Path PASTA_DOC = Paths.get("config", "docs");
	private static final Path PASTA_LOGO = Paths.get("config", "logos");

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	private FileStorageFactory() {
	}

	public static String salvarArquivo(File arquivoOriginal, String tipoDocumento) throws IOException {

		if (Files.notExists(PASTA_DOC)) {
			Files.createDirectories(PASTA_DOC);
		}

		String timestamp = LocalDateTime.now().format(FORMATO_DATA);
		String nomeLimpo = tipoDocumento.trim().replaceAll("\\s+", "_");
		String extensao = getExtensao(arquivoOriginal.getName());

		String nomeFinal = nomeLimpo + "_" + timestamp + extensao;

		Path destino = PASTA_DOC.resolve(nomeFinal);

		Files.copy(arquivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
		return destino.toString();
	}
	
	public static String salvarLogo(File arquivoOriginal, String nomeFornecedor) throws IOException{

		if (Files.notExists(PASTA_LOGO)) {
			Files.createDirectories(PASTA_LOGO);
		}

		String extensao = getExtensao(arquivoOriginal.getName());
		String nomeFinal = "logo_empresa_"+nomeFornecedor.trim().replaceAll("\\s+", "_") + extensao;

		Path destino = PASTA_LOGO.resolve(nomeFinal);

		Files.copy(arquivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
		return destino.toString();
	}
	
	public static void abrirArquivo(String caminho)throws IOException{
		if (caminho==null||caminho.isBlank()) {
			throw new IOException("O CAMINHO DO ARQUIVO ESTÁ VAZIO OU INVÁLIDO.");
		}
		File arquivo = new File(caminho);
		
		if (!arquivo.exists()) {
			throw new IOException("O ARQUIVO NÃO FOI ENCONTRADO NO CAMINHO: "+caminho);
		}
		
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(arquivo);
		}else {
			throw new IOException("A ABERTURA DE ARQUIVOS NÃO É SUPORTADA NESTE SISTEMA.");
		}
	}

	private static String getExtensao(String nome) {
		return nome.contains(".") ? nome.substring(nome.lastIndexOf('.')) : "";
	}

}
