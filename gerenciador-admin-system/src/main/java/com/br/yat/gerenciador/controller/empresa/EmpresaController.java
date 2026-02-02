package com.br.yat.gerenciador.controller.empresa;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.controller.BaseController;
import com.br.yat.gerenciador.controller.RefreshCallback;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.EmpresaDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.AppEventManager;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.EmpresaView;

public class EmpresaController extends BaseController {

	private final EmpresaView view;
	private final EmpresaService service;
	private final TipoCadastro tipoCadastro;

	private RefreshCallback refreshCallback;

	private final DadoPrincipalController ePrincipal;
	private final DadoEnderecoController eEndereco;
	private final DadoContatoController eContato;
	private final DadoFiscalController eFiscal;
	private final DadoRepresentanteController eRepresentante;
	private final DadoBancarioController eBancario;
	private final DadoComplementarController eComplementar;

	private static final Map<TipoCadastro, List<String>> ABAS = Map.of(TipoCadastro.CLIENTE,
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS"), TipoCadastro.FORNECEDORA,
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS", "DADOS FISCAIS", "REPRESENTANTE LEGAL",
					"DADOS BANCÁRIOS", "INFORMAÇÕES COMPLEMENTARES"));

	public EmpresaController(EmpresaView view, EmpresaService service, DadoPrincipalController ePrincipal,
			DadoEnderecoController eEndereco, DadoContatoController eContato, DadoFiscalController eFiscal,
			DadoRepresentanteController eRepresentante, DadoBancarioController eBancario,
			DadoComplementarController eComplementar, TipoCadastro tipoCadastro) {

		this.view = view;
		this.service = service;
		this.tipoCadastro = tipoCadastro;
		this.ePrincipal = ePrincipal;
		this.eEndereco = eEndereco;
		this.eContato = eContato;
		this.eFiscal = eFiscal;
		this.eRepresentante = eRepresentante;
		this.eBancario = eBancario;
		this.eComplementar = eComplementar;

		inicializar();
	}

	private void inicializar() {
		configuracaoInicial();
		registrarAcoes();
		atualizarAbas();
		inicializarPorTipo();
	}

	private void configuracaoInicial() {
		view.getDadoPrincipal().setTipoCadastro(tipoCadastro);
		view.getDadoPrincipal().getCbTipoCadastro().setEnabled(false);
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			view.getBtnNovo().setEnabled(false);
			view.getBtnNovo().setToolTipText("CADASTRO ÚNICO DE FORNECEDORA NÃO PERMITE NOVOS REGISTROS.");
		}
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}

	private void inicializarPorTipo() {
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			carregarDados(0);
			setModoEdicao(true);
		} else {
			limparFormulario();
			setModoEdicao(false);
		}
	}

	public void carregarDados(int id) {
		runAsync(SwingUtilities.getWindowAncestor(view),
				() -> service.carregarEmpresaCompleta(id, tipoCadastro, Sessao.getUsuario()), dados -> {
					if (dados == null) {
						if (tipoCadastro == TipoCadastro.CLIENTE)
							DialogFactory.erro(view, "ERRO: EMPRESA NÃO ENCONTRADA.");
						return;
					}

					Empresa empresa = dados.empresa();
					if (!empresa.isAtivo()) {
						DialogFactory.aviso(view, "ESTE REGISTRO ESTÁ INATIVO E NÃO PODE SER ALTERADO.");
						view.doDefaultCloseAction();
						return;
					}

					preencherFormulario(dados);
					setModoEdicao(true);
				});
	}

	private void preencherFormulario(EmpresaDTO dto) {
		Empresa emp = dto.empresa();

		ePrincipal.setDados(emp);
		if (emp.getEndereco() != null)
			eEndereco.setDados(emp.getEndereco());

		eContato.setDados(dto.contatos());

		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			eFiscal.setDadosComplementar(emp);
			eRepresentante.setDados(dto.representantes());
			eBancario.setDados(dto.bancos());
			eComplementar.setDados(dto.complementar(), dto.documentos());
		}
		atualizarTextoSalvar();
	}

	public void prepararNovo() {
		limparFormulario();
		setModoEdicao(true);
	}

	private void registrarAcoes() {
		view.getBtnSalvar().addActionListener(e -> salvar());
		view.getBtnNovo().addActionListener(e -> prepararNovo());
		view.getBtnCancelar().addActionListener(e -> view.doDefaultCloseAction());
	}

	private void salvar() {
		if (!validarFormulario())
			return;

		Empresa empresa = ePrincipal.getDados();

		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			eFiscal.getDadosComplementar(empresa);
		}

		boolean alterar = empresa.getIdEmpresa() > 0;
		Usuario executor = Sessao.getUsuario();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarEmpresaCompleta(eEndereco.getDados(), empresa, eContato.getDados(),
					tipoCadastro == TipoCadastro.FORNECEDORA ? eRepresentante.getDados() : null,
					tipoCadastro == TipoCadastro.FORNECEDORA ? eBancario.getDados() : null,
					tipoCadastro == TipoCadastro.FORNECEDORA ? eComplementar.getComplementar() : null,
					tipoCadastro == TipoCadastro.FORNECEDORA ? eComplementar.getDocumentos() : null, executor);
			return true;
		}, ok -> {
			DialogFactory.informacao(view, alterar ? "ALTERADO COM SUCESSO." : "SALVO COM SUCESSO.");
			if (tipoCadastro == TipoCadastro.FORNECEDORA) {
		        AppEventManager.notifyLogoChange();
		    }
			
			if (tipoCadastro == TipoCadastro.CLIENTE) {
				limparFormulario();
				setModoEdicao(false);
				view.getBtnNovo().setEnabled(true);
			}
			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();
		});
	}

	private boolean validarFormulario() {
		if (!ePrincipal.isValido())
			return focar("DADOS PRINCIPAIS");
		if (!eEndereco.isValido())
			return focar("ENDEREÇO");
		if (!eContato.isValido())
			return focar("CONTATOS");

		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			if (!eFiscal.isValido())
				return focar("DADOS FISCAIS");
			if (!eRepresentante.isValido())
				return focar("REPRESENTANTE LEGAL");
			if (!eBancario.isValido())
				return focar("DADOS BANCÁRIOS");
			if (!eComplementar.isValido())
				return focar("INFORMAÇÕES COMPLEMENTARES");
		}
		return true;
	}

	private boolean focar(String aba) {
		JPanel panel = view.getPanelByName(aba);
		if (panel != null)
			view.getTabbedPane().setSelectedComponent(panel);
		return false;
	}

	private void limparFormulario() {
		ePrincipal.limpar();
		eEndereco.limpar();
		eContato.limpar();
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			eFiscal.limpar();
			eRepresentante.limpar();
			eBancario.limpar();
			eComplementar.limpar();
		}
		view.getTabbedPane().setSelectedIndex(0);
		atualizarTextoSalvar();
	}

	private void setModoEdicao(boolean ativo) {
		boolean temPermissaoSalvar = Sessao.getUsuario().isMaster()
				|| Sessao.getPermissoes().contains(MenuChave.CADASTROS_EMPRESA_CLIENTE);

		ePrincipal.desativarAtivar(ativo);
		eEndereco.desativarAtivar(ativo);
		eContato.desativarAtivar(ativo);

		view.getBtnSalvar().setEnabled(ativo && temPermissaoSalvar);
		view.getBtnNovo().setEnabled(!ativo && tipoCadastro == TipoCadastro.CLIENTE && temPermissaoSalvar);
	}

	private void atualizarAbas() {
		view.getTabbedPane().removeAll();
		ABAS.get(tipoCadastro).forEach(nome -> {
			JPanel panel = view.getPanelByName(nome);
			if (panel != null)
				view.getTabbedPane().addTab(nome, panel);
		});
	}

	private void atualizarTextoSalvar() {
		boolean alterar = ePrincipal.getDados().getIdEmpresa() > 0;
		view.getBtnSalvar().setText(alterar ? "ALTERAR" : "SALVAR");
	}

	public boolean podeFechar() {
		if (!view.getBtnSalvar().isEnabled())
			return true;

		String razao = ePrincipal.getDados().getRazaoSocialEmpresa();
		return ValidationUtils.isEmpty(razao)
				|| DialogFactory.confirmacao(view, "EXISTEM ALTERAÇÕES NÃO SALVAS. DESEJA REALMENTE SAIR?");
	}

}
