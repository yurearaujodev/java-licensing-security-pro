package com.br.yat.gerenciador.controller.empresa;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.controller.BaseController;
import com.br.yat.gerenciador.controller.RefreshCallback;
import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.dto.EmpresaDTO;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.EmpresaView;

public class EmpresaController extends BaseController {
	private final EmpresaView view;
	private final EmpresaService service;
	private final DadoPrincipalController ePrincipal;
	private final DadoEnderecoController eEndereco;
	private final DadoContatoController eContato;
	private final DadoFiscalController eFiscal;
	private final DadoRepresentanteController eRepresentante;
	private final DadoBancarioController eBancario;
	private final DadoComplementarController eComplementar;
	private RefreshCallback refreshCallback;

	private final TipoCadastro tipoCadastro;

	private static final Map<TipoCadastro, List<String>> abasPorTipo = Map.of(TipoCadastro.CLIENTE,
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS"), TipoCadastro.FORNECEDORA,
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS", "DADOS FISCAIS", "REPRESENTANTE LEGAL",
					"DADOS BANCÁRIOS", "INFORMAÇÕES COMPLEMENTARES"));

	public EmpresaController(EmpresaView view, EmpresaService service, DadoPrincipalController ePrincipal,
			DadoEnderecoController eEndereco, DadoContatoController eContato, DadoFiscalController eFiscal,
			DadoRepresentanteController eRepresentante, DadoBancarioController eBancario,
			DadoComplementarController eComplementar, TipoCadastro tipoCadastro) {
		this.view = view;
		this.service = service;
		this.ePrincipal = ePrincipal;
		this.eEndereco = eEndereco;
		this.eContato = eContato;
		this.eFiscal = eFiscal;
		this.eRepresentante = eRepresentante;
		this.eBancario = eBancario;
		this.eComplementar = eComplementar;

		this.tipoCadastro = tipoCadastro;

		configuracaoInicial();
		registrarAcoes();
		atualizarAbas(tipoCadastro);
		inicializarPortipo();
	}

	private void inicializarPortipo() {
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			carregarDados();
			setModoAlterar(true);
		} else {
			limparFormulario();
			setModoAlterar(false);

		}
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}

	private void carregarDados() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			return service.carregarForncedoraCompleta();
		}, dados -> {
			if (dados != null)
				preencherFormulario(dados);
		});
	}

	public void carregarDadosCliente(int id) {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.carregarClienteCompleto(id), dados -> {
			if (dados != null && dados.empresa() != null) {
				preencherFormulario(dados);
				setModoAlterar(true);
			} else {
				DialogFactory.erro(view, "ERRO: EMPRESA NÃO ENCONTRADA.");
			}
		});
	}

	private void preencherFormulario(EmpresaDTO dados) {
		Empresa emp = dados.empresa();

		ePrincipal.setDados(emp);

		if (emp.getEndereco() != null) {
			eEndereco.setDados(emp.getEndereco());
		}

		eContato.setDados(dados.contatos());
		if (emp.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
			eFiscal.setDadosComplementar(emp);
			eRepresentante.setDados(dados.representantes());
			eBancario.setDados(dados.bancos());
			eComplementar.setDados(dados.complementar(), dados.documentos());
		}
		atualizarTextoSalvar();
	}

	public void prepararNovo() {
		limparFormulario();
		setModoAlterar(true);
		view.getBtnNovo().setEnabled(false);
	}

	private void configuracaoInicial() {
		view.getDadoPrincipal().setTipoCadastro(tipoCadastro);
		view.getDadoPrincipal().getCbTipoCadatro().setEnabled(false);
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			view.getBtnNovo().setEnabled(false);
			view.getBtnNovo().setToolTipText("CADASTRO ÚNICO DE FORNECEDORA NÃO PERMITE NOVOS REGISTROS.");
		}
	}

	private void registrarAcoes() {
		view.getBtnSalvar().addActionListener(e -> aoClicarSalvar());
		view.getBtnNovo().addActionListener(e -> aoClicarNovo());
		view.getBtnCancelar().addActionListener(e -> view.doDefaultCloseAction());
	}

	private void aoClicarNovo() {
		limparFormulario();
		setModoAlterar(true);
		view.getBtnNovo().setEnabled(false);
	}

	private void setModoAlterar(boolean ativa) {
		ePrincipal.desativarAtivar(ativa);
		eEndereco.desativarAtivar(ativa);
		eContato.desativarAtivar(ativa);

		view.getBtnSalvar().setEnabled(ativa);
		view.getBtnCancelar().setEnabled(true);

		if (tipoCadastro == TipoCadastro.FORNECEDORA) {

			view.getBtnNovo().setEnabled(false);
		}

		if (tipoCadastro == TipoCadastro.CLIENTE) {
			boolean isAlterar = ePrincipal.getDados().getIdEmpresa() > 0;
			view.getBtnNovo().setEnabled(isAlterar ? false : !ativa);
		}
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
		view.getBtnSalvar().setText("SALVAR");
		view.getTabbedPane().setSelectedIndex(0);
		atualizarTextoSalvar();
	}

	private void atualizarAbas(TipoCadastro tipo) {
		view.getTabbedPane().removeAll();
		List<String> abas = abasPorTipo.getOrDefault(tipo, List.of());
		for (String aba : abas) {
			JPanel panel = view.getPanelByName(aba);
			if (panel != null) {
				view.getTabbedPane().addTab(aba, panel);
			}
		}
	}

	private void aoClicarSalvar() {
		if (!validarFormulario())
			return;

		final Endereco endereco = eEndereco.getDados();
		final Empresa empresa = ePrincipal.getDados();
		final List<Contato> contatos = eContato.getDados();

		List<Representante> representantes = null;
		List<Banco> bancos = null;
		Complementar complementar = null;
		List<Documento> documentos = null;

		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			eFiscal.getDadosComplementar(empresa);

			representantes = eRepresentante.getDados();
			bancos = eBancario.getDados();
			complementar = eComplementar.getComplementar();
			documentos = eComplementar.getDocumentos();
		}

		final List<Representante> finalReps = representantes;
		final List<Banco> finalBancos = bancos;
		final Complementar finalComp = complementar;
		final List<Documento> finalDocs = documentos;

		final boolean isAlterar = empresa.getIdEmpresa() > 0;
		final String mensagem = isAlterar ? "ALTERADO COM SUCESSO." : "SALVO COM SUCESSO.";

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarEmpresaCompleta(endereco, empresa, contatos, finalReps, finalBancos, finalComp, finalDocs);
			return true;
		}, success -> {
			DialogFactory.informacao(view, mensagem);
			if (tipoCadastro == TipoCadastro.CLIENTE) {
				limparFormulario();
				setModoAlterar(false);
			}
			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();
		});
	}

	private void focar(String Aba) {
		var panel = view.getPanelByName(Aba);
		if (panel != null) {
			view.getTabbedPane().setSelectedComponent(panel);
		}
	}

	private boolean validarFormulario() {
		if (!ePrincipal.isValido()) {
			focar("DADOS PRINCIPAIS");
			return false;
		}
		if (!eEndereco.isValido()) {
			focar("ENDEREÇO");
			return false;
		}
		if (!eContato.isValido()) {
			focar("CONTATOS");
			return false;
		}
		if (tipoCadastro == TipoCadastro.FORNECEDORA) {
			if (!eFiscal.isValido()) {
				focar("DADOS FISCAIS");
				return false;
			}
			if (!eRepresentante.isValido()) {
				focar("REPRESENTANTE LEGAL");
				return false;
			}
			if (!eBancario.isValido()) {
				focar("DADOS BANCÁRIOS");
				return false;
			}
			if (!eComplementar.isValido()) {
				focar("INFORMAÇÕES COMPLEMENTARES");
				return false;
			}

		}
		return true;
	}
	
	private void atualizarTextoSalvar() {
		boolean isAlterar = ePrincipal.getDados().getIdEmpresa()>0;
		view.getBtnSalvar().setText(isAlterar?"ALTERAR":"SALVAR");
	}

	public boolean podeFechar() {
		if (!view.getBtnSalvar().isEnabled()) {
			return true;
		}
		if (isFormularioVazio()) {
			return true;
		}

		return DialogFactory.confirmacao(view, "EXISTEM ALTERAÇÕES NÃO SALVAS. DESEJA REALMENTE SAIR?");
	}

	private boolean isFormularioVazio() {
		var razao = ePrincipal.getDados().getRazaoSocialEmpresa();
		return (ePrincipal.getDados().getIdEmpresa() <= 0) && (ValidationUtils.isEmpty(razao));
	}
}
