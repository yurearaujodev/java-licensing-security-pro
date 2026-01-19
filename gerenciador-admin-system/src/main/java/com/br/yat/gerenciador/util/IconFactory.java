package com.br.yat.gerenciador.util;

import java.awt.Image;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
/**
 * Classe utilitária para carregamento e fornecimento de ícones da aplicação.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Carregar ícones a partir de recursos internos.</li>
 * <li>Redimensionar ícones para tamanhos específicos.</li>
 * <li>Manter cache de ícones para evitar carregamento repetido.</li>
 * </ul>
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */
public final class IconFactory {

	private static final Map<String, Icon> CACHE = new ConcurrentHashMap<>();

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private IconFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Retorna um ícone carregado e redimensionado, utilizando cache para evitar recarregamento.
	 * 
	 * @param caminho caminho do recurso de imagem
	 * @param largura largura desejada
	 * @param altura altura desejada
	 * @return ícone carregado e redimensionado
	 * @throws IllegalArgumentException se o recurso não for encontrado
	 */
	public static Icon icon(String caminho, int largura, int altura) {
		String chave = caminho + "_" + largura + "x" + altura;
		return CACHE.computeIfAbsent(chave, k -> carregarIcone(caminho, largura, altura));
	}

	/**
	 * Carrega um ícone a partir de recurso interno e redimensiona.
	 * 
	 * @param caminho caminho do recurso de imagem
	 * @param largura largura desejada
	 * @param altura altura desejada
	 * @return ícone carregado e redimensionado
	 * @throws IllegalArgumentException se o recurso não for encontrado
	 */
	private static Icon carregarIcone(String caminho, int largura, int altura) {
		URL url = IconFactory.class.getResource(caminho);
		if (url == null) {
			throw new IllegalArgumentException("ICONE NÃO ENCONTRADO: " + caminho);
		}
		Image imagem = new ImageIcon(url).getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);

		return new ImageIcon(imagem);
	}

	/**
	 * Retorna ícone padrão de usuário.
	 * 
	 * @return ícone de usuário
	 */
	public static Icon usuario() {
		return icon("/image/user.png", 60, 60);
	}

	/**
	 * Retorna ícone padrão de dados.
	 * 
	 * @return ícone de dados
	 */
	public static Icon data() {
		return icon("/image/data.png", 60, 60);
	}

	/**
	 * Retorna ícone padrão de logotipo.
	 * 
	 * @return ícone de logotipo
	 */
	public static Icon logo() {
		return icon("/image/logotipo.png", 150, 150);
	}
	
	public static Icon salvar() {
		return icon("/image/salvar_24.png", 25, 25);
	}
	
	public static Icon cancelar() {
		return icon("/image/cancelar_24.png", 25, 25);
	}
	
	public static Icon novo() {
		return icon("/image/novo_24.png", 25, 25);
	}

}
