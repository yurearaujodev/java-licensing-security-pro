package com.br.yat.gerenciador.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class IconFactory {

	private static final Map<String, Icon> CACHE = new ConcurrentHashMap<>();

	private IconFactory() {
		throw new AssertionError();
	}

	public static Icon icon(String caminho, int largura, int altura) {
		String chave = caminho + "_" + largura + "x" + altura;
		return CACHE.computeIfAbsent(chave, k -> carregarIcone(caminho, largura, altura));
	}

	private static Icon carregarIcone(String caminho, int largura, int altura) {
		URL url = IconFactory.class.getResource(caminho);
		if (url == null)
			throw new IllegalArgumentException("ICONE NÃO ENCONTRADO: " + caminho);
		return redimensionarComQualidade(new ImageIcon(url).getImage(), largura, altura);
	}

	public static Icon externalIcon(String caminho, int largura, int altura) {
		if (caminho == null || caminho.isBlank())
			return logo();

		String chave = "EXTERNAL_" + caminho + "_" + largura + "x" + altura;

		return CACHE.computeIfAbsent(chave, k -> {
			File file = new File(caminho);
			if (!file.exists())
				return logo();

			try {
				BufferedImage imgDisk = ImageIO.read(file);

				if (imgDisk != null) {
					return redimensionarComQualidade(imgDisk, largura, altura);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return logo();
		});
	}

	private static Icon redimensionarComQualidade(Image imgOriginal, int largura, int altura) {
		BufferedImage bimg = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bimg.createGraphics();

		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, largura, altura);
		g2.setComposite(AlphaComposite.SrcOver);
		// ------------------------------------------------------------------------

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.drawImage(imgOriginal, 0, 0, largura, altura, null);
		g2.dispose();

		return new ImageIcon(bimg);
	}

	public static void limparCacheLogo() {
		CACHE.keySet().removeIf(key -> key.startsWith("EXTERNAL"));
	}

	public static Icon logo() {
		return icon("/image/logotipo.png", 160, 160);
	}

	public static Icon usuario() {
		return icon("/image/user.png", 60, 60);
	}

	public static Icon data() {
		return icon("/image/data.png", 60, 60);
	}
	public static Icon dataHora() {
		return icon("/image/data.png", 24, 24);
	}

	public static Icon logout() {
		return icon("/image/logout.png", 36, 36);
	}

	public static Icon bancoOk() {
		return icon("/image/dbok.png", 16, 16);
	}

	public static Icon bancoErro() {
		return icon("/image/dberror.png", 16, 16);
	}

	public static Icon novo() {
		return icon("/image/novo_24.png", 24, 24);
	}

	public static Icon salvar() {
		return icon("/image/salvar_24.png", 24, 24);
	}

	public static Icon cancelar() {
		return icon("/image/cancelar_24.png", 24, 24);
	}

	public static Icon pesquisar() {
		return icon("/image/pesquisar_24.png", 24, 24);
	}
}