package com.br.yat.gerenciador.controller;

import java.util.List;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.LoginDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.service.LogSistemaService;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class LoginController extends BaseController {
	private final UsuarioViewLogin view;
	private final UsuarioService userService;
	private final AutenticacaoService authService;

	public LoginController(UsuarioViewLogin view, AutenticacaoService authService, UsuarioService userService) {
		this.view = view;
		this.userService = userService;
		this.authService = authService;
		registrarAcoes();
		SwingUtilities.invokeLater(view::focarEmail);
	}

	private void registrarAcoes() {
		view.getBtnEntrar().addActionListener(e -> {
			String email = view.getEmail();
			char[] senha = view.getSenha();

			view.limpar();

			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				Usuario user = authService.autenticar(email, senha);

				List<MenuChave> permissoes = userService.carregarPermissoesAtivas(user.getIdUsuario());
				return new LoginDTO(user, permissoes);
			}, data -> {
				Usuario user = data.user();

				if (user.isForcarResetSenha()) {
					ViewFactory.abrirTrocaSenhaObrigatoria(user, () -> {
						finalizarLogin(data);
					});
					view.dispose();
					return;
				}

				finalizarLogin(data);
			});
			view.limpar();
		});

		view.getBtnEsqueciSenha().addActionListener(e -> {
			DialogFactory.informacao(view,
					"POR FAVOR, ENTRE EM CONTATO COM O ADMINISTRADOR DO SISTEMA PARA SOLICITAR O RESET DE SUA SENHA.");
		});
	}

	private void finalizarLogin(LoginDTO data) {
		ParametroSistemaService ps = new ParametroSistemaService();
		int tempoSessao = ps.getInt(ParametroChave.TEMPO_SESSAO_MIN, 30);

		Sessao.login(data.user(), data.permissoes(), tempoSessao);
		if (ViewFactory.getMainController() != null) {
			ViewFactory.getMainController().iniciarMonitorSessao();
		}
		if (UsuarioPolicy.isPrivilegiado(data.user())) {
			runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
				new LogSistemaService().executarLimpezaAutomatica(data.user());
				return null;
			}, result -> {
			});
		}
		ViewFactory.atualizarAcoesMenuPrincipal();
		if (view != null) {
			view.dispose();
		}
	}
}