package com.br.yat.gerenciador.view.factory;

import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.br.yat.gerenciador.controller.MenuPrincipalController;
import com.br.yat.gerenciador.controller.empresa.DadoBancarioController;
import com.br.yat.gerenciador.controller.empresa.DadoComplementarController;
import com.br.yat.gerenciador.controller.empresa.DadoContatoController;
import com.br.yat.gerenciador.controller.empresa.DadoEnderecoController;
import com.br.yat.gerenciador.controller.empresa.DadoFiscalController;
import com.br.yat.gerenciador.controller.empresa.DadoPrincipalController;
import com.br.yat.gerenciador.controller.empresa.DadoRepresentanteController;
import com.br.yat.gerenciador.controller.empresa.EmpresaConsultaController;
import com.br.yat.gerenciador.controller.empresa.EmpresaController;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;

public final class ViewFactory {

	public static EmpresaView createEmpresaView(TipoCadastro tipoCadastro) {
		EmpresaView view = new EmpresaView();
		EmpresaService service = new EmpresaService();

		var ePrincipal = new DadoPrincipalController(view.getDadoPrincipal());
		var eEndereco = new DadoEnderecoController(view.getDadoEndereco());
		var eContato = new DadoContatoController(view.getDadoContato());
		var eFiscal = new DadoFiscalController(view.getDadoFiscal());
		var eRepresentante = new DadoRepresentanteController(view.getDadoRepresentante());
		var eBancario = new DadoBancarioController(view.getDadoBancario());
		var eComplementar = new DadoComplementarController(view.getDadoComplementar());

		EmpresaController controller = new EmpresaController(view, service, ePrincipal, eEndereco, eContato, eFiscal,
				eRepresentante, eBancario, eComplementar, tipoCadastro);

		view.putClientProperty("controller", controller);

		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				if (controller.podeFechar()) {
					controller.dispose();
					view.dispose();
				}
			}
		});

		return view;
	}

	public static MenuPrincipal createMenuPrincipal() {
		MenuPrincipal view = new MenuPrincipal();
		new MenuPrincipalController(view);
		return view;
	}

	public static EmpresaConsultaView createEmpresaConsultaView() {
		EmpresaConsultaView view = new EmpresaConsultaView();
		EmpresaService service = new EmpresaService();
		EmpresaConsultaController controller = new EmpresaConsultaController(view, service);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				controller.dispose();
			}
		});
		return view;
	}

	public static EmpresaView createEmpresaEdicaoView(int id) {
		EmpresaView view = createEmpresaView(TipoCadastro.CLIENTE);
		view.setTitle("CARREGANDO DADOS... POR FAVOR, AGUARDE.");

		EmpresaController controller = (EmpresaController) view.getClientProperty("controller");
		controller.carregarDadosCliente(id);
		return view;
	}

}
