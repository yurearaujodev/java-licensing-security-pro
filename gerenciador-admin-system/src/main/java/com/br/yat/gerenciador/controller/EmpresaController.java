package com.br.yat.gerenciador.controller;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.EmpresaDTO;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
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
	private JDialog dialogLoading;
	private RefreshCallback refreshCallback;

	private final String tipoCadastro;

	private static final Map<String, List<String>> abasPorTipo = Map.of("CLIENTE",
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS"), "FORNECEDORA",
			List.of("DADOS PRINCIPAIS", "ENDEREÇO", "CONTATOS", "DADOS FISCAIS", "REPRESENTANTE LEGAL",
					"DADOS BANCÁRIOS", "INFORMAÇÕES COMPLEMENTARES"));

	public EmpresaController(EmpresaView view, EmpresaService service, DadoPrincipalController ePrincipal,
			DadoEnderecoController eEndereco, DadoContatoController eContato, DadoFiscalController eFiscal,
			DadoRepresentanteController eRepresentante, DadoBancarioController eBancario,
			DadoComplementarController eComplementar, String tipoCadastro) {
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
		if ("FORNECEDORA".equals(tipoCadastro)) {
			carregarDados();
			desativarAtiva(true);
		} else {
			limparFormulario();
			desativarAtiva(false);
			
		}
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}

	private void carregarDados() {
		setLoading(true);
		executor.submit(() -> {
			try {
				EmpresaDTO dados = service.carregarForncedoraCompleta();

				SwingUtilities.invokeLater(() -> {
					if (dados != null) {
						preencherFormulario(dados);
					}
					setLoading(false);
				});

			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					setLoading(false);
					DialogFactory.erro(view, "ERRO AO CARREGAR DADOS: " + e.getMessage());
				});
			}
		});
	}

	public void carregarDadosCliente(int id) {
		setLoading(true);
		executor.submit(() -> {
			try {
				EmpresaDTO dados = service.carregarClienteCompleto(id);

				SwingUtilities.invokeLater(() -> {
					if (dados != null && dados.empresa() != null) {
						preencherFormulario(dados);
						desativarAtiva(true);
						setLoading(false);
					} else {
						setLoading(false);
						DialogFactory.erro(view, "ERRO: EMPRESA NÃO ENCONTRADA.");
					}
				});

			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					setLoading(false);
					DialogFactory.erro(view, "ERRO AO CARREGAR DADOS: " + e.getMessage());
					view.dispose();
				});
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
		if ("FORNECEDORA".equals(emp.getTipoEmpresa())) {
			eFiscal.setDadosComplementar(emp);
			eRepresentante.setDados(dados.representantes());
			eBancario.setDados(dados.bancos());
			eComplementar.setDados(dados.complementar(), dados.documentos());
		}
	}

	public void prepararNovo() {
		limparFormulario();
		desativarAtiva(true);
		view.getBtnNovo().setEnabled(false);
	}

	private void configuracaoInicial() {
		view.getDadoPrincipal().setTipoCadastro(tipoCadastro);
		view.getDadoPrincipal().getCbTipoCadatro().setEnabled(false);
		if ("FORNECEDORA".equals(tipoCadastro)) {
			view.getBtnNovo().setEnabled(false);
			view.getBtnNovo().setToolTipText("CADASTRO ÚNICO DE FORNECEDORA NÃO PERMITE NOVOS REGISTROS.");
		}
	}

	private void registrarAcoes() {
		view.getBtnSalvar().addActionListener(e -> aoClicarSalvar());
		view.getBtnNovo().addActionListener(e -> aoClicarNovo());
		view.getBtnCancelar().addActionListener(e -> aoClicarCancelar());
	}

	private void aoClicarCancelar() {
		view.doDefaultCloseAction();
	}

	private void aoClicarNovo() {
		limparFormulario();
		desativarAtiva(true);
		view.getBtnNovo().setEnabled(false);
	}

	private void desativarAtiva(boolean ativa) {
		ePrincipal.desativarAtivar(ativa);
		eEndereco.desativarAtivar(ativa);
		eContato.desativarAtivar(ativa);

		view.getBtnSalvar().setEnabled(ativa);
		view.getBtnCancelar().setEnabled(true);
		

		if ("FORNECEDORA".equals(tipoCadastro)) {

			view.getBtnNovo().setEnabled(false);
		}

		if ("CLIENTE".equals(tipoCadastro)) {
			boolean isAlterar = ePrincipal.getDados().getIdEmpresa() > 0;
			view.getBtnNovo().setEnabled(isAlterar ? false : !ativa);
		}
	}

	private void limparFormulario() {
		ePrincipal.limpar();
		eEndereco.limpar();
		eContato.limpar();

		if ("FORNECEDORA".equals(tipoCadastro)) {
			eFiscal.setDadosComplementar(new Empresa());
			eRepresentante.setDados(Collections.emptyList());
			eBancario.setDados(Collections.emptyList());
			eComplementar.setDados(new Complementar(), Collections.emptyList());
		}
		view.getBtnSalvar().setText("SALVAR");
		view.getTabbedPane().setSelectedIndex(0);
	}

	private void atualizarAbas(String tipo) {
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

		final List<Representante> representantes;
		final List<Banco> bancos;
		final Complementar complementar;
		final List<Documento> documentos;

		if ("FORNECEDORA".equals(tipoCadastro)) {
			eFiscal.getDadosComplementar(empresa);

			representantes = eRepresentante.getDados();
			bancos = eBancario.getDados();
			complementar = eComplementar.getComplementar();
			documentos = eComplementar.getDocumentos();
		} else {
			representantes = null;
			bancos = null;
			complementar = null;
			documentos = null;
		}

		final boolean isAlterar = empresa.getIdEmpresa() > 0;
		final String mensagem = isAlterar ? "ALTERADO COM SUCESSO." : "SALVO COM SUCESSO.";
		setLoading(true);
		executor.submit(() -> {
			try {

				service.salvarEmpresaCompleta(endereco, empresa, contatos, representantes, bancos, complementar,
						documentos);
				SwingUtilities.invokeLater(() -> {
					setLoading(false);
					DialogFactory.informacao(view, mensagem);
					if ("CLIENTE".equals(tipoCadastro)) {
						limparFormulario();
						desativarAtiva(false);
					}

					if (refreshCallback != null) {
						refreshCallback.onSaveSuccess();
					}
				});
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					setLoading(false);
					DialogFactory.erro(view, e.getMessage());
				});
			}
		});
	}

	private void focarAba(String nomeAba) {
		var panel = view.getPanelByName(nomeAba);
		if (panel != null) {
			view.getTabbedPane().setSelectedComponent(panel);
		}
	}

	private boolean validarFormulario() {
		if (!ePrincipal.isValido()) {
			focarAba("DADOS PRINCIPAIS");
			return false;
		}
		if (!eEndereco.isValido()) {
			focarAba("ENDEREÇO");
			return false;
		}
		if (!eContato.isValido()) {
			focarAba("CONTATOS");
			return false;
		}
		if ("FORNECEDORA".equals(tipoCadastro)) {
			if (!eFiscal.isValido()) {
				focarAba("DADOS FISCAIS");
				return false;
			}
			if (!eRepresentante.isValido()) {
				focarAba("REPRESENTANTE LEGAL");
				return false;
			}
			if (!eBancario.isValido()) {
				focarAba("DADOS BANCÁRIOS");
				return false;
			}
			if (!eComplementar.isValido()) {
				focarAba("INFORMAÇÕES COMPLEMENTARES");
				return false;
			}

		}
		return true;
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

	private void setLoading(boolean carregando) {
		SwingUtilities.invokeLater(() -> {
			view.getBtnSalvar().setEnabled(!carregando);

			var progressBar = DesktopFactory.createProgressBar();
			progressBar.setIndeterminate(carregando);
			progressBar.setVisible(carregando);
			if (carregando) {
				if (dialogLoading == null)
					criarDialogLoading();
				view.getBtnSalvar().setText("PROCESSANDO...");

				Thread.ofVirtual().start(() -> {
					if (dialogLoading != null)
						dialogLoading.setVisible(true);
				});
			} else {
				if (dialogLoading != null) {
					dialogLoading.setVisible(false);
					boolean isAlterar = ePrincipal.getDados().getIdEmpresa() > 0;
					String textoBotao = (isAlterar ? "ALTERAR" : "SALVAR");
					view.getBtnSalvar().setText(textoBotao);
					if (isAlterar) {
						view.getBtnNovo().setEnabled(false);
					}
				}
			}

		});
	}

	private void criarDialogLoading() {
		Window parentWindow = SwingUtilities.getWindowAncestor(view);

		dialogLoading = new JDialog((Frame) parentWindow, "PROCESSANDO", Dialog.ModalityType.APPLICATION_MODAL);
		var panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		var label = LabelFactory.createLabel("AGUARDE, PROCESSANDO INFORMAÇÕES...");
		var progressBar = DesktopFactory.createProgressBar();
		progressBar.setIndeterminate(true);

		panel.add(label, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.CENTER);

		dialogLoading.add(panel);
		dialogLoading.pack();
		dialogLoading.setLocationRelativeTo(view);
		dialogLoading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
}
