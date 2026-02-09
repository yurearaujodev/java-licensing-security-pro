package com.br.yat.gerenciador.controller;

import java.util.List;

import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.LoginDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.LogSistemaService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class LoginController extends BaseController {
	private final UsuarioViewLogin view;
	private final UsuarioService service;

	public LoginController(UsuarioViewLogin view) {
		this.view = view;
		this.service = new UsuarioService();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getBtnEntrar().addActionListener(e -> {
			String email = view.getEmail();
			char[] senha = view.getSenha();

			view.limpar();

			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				Usuario user = service.autenticar(email, senha);
				List<MenuChave> permissoes = service.carregarPermissoesAtivas(user.getIdUsuario());
				return new LoginDTO(user, permissoes);
			}, data -> {
				Sessao.login(data.user(), data.permissoes());
				if (data.user().isMaster()) {
					runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
						new LogSistemaService().executarLimpezaAutomatica(data.user());
						return null;
					}, result -> {
						// Logamos apenas no console/arquivo via SLF4J (da BaseController)
						// para confirmar que a virtual thread terminou.
					});
				}
				ViewFactory.atualizarAcoesMenuPrincipal();
				view.dispose();
			});
		});
		view.getBtnEsqueciSenha().addActionListener(e -> {
			DialogFactory.informacao(view,
					"POR FAVOR, ENTRE EM CONTATO COM O ADMINISTRADOR DO SISTEMA PARA SOLICITAR O RESET DE SUA SENHA.");
		});
	}
}
