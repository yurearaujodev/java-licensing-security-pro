package com.br.yat.gerenciador.util.ui;

import com.br.yat.gerenciador.controller.DadoBancarioController;
import com.br.yat.gerenciador.controller.DadoComplementarController;
import com.br.yat.gerenciador.controller.DadoContatoController;
import com.br.yat.gerenciador.controller.DadoEnderecoController;
import com.br.yat.gerenciador.controller.DadoFiscalController;
import com.br.yat.gerenciador.controller.DadoPrincipalController;
import com.br.yat.gerenciador.controller.DadoRepresentanteController;
import com.br.yat.gerenciador.controller.EmpresaController;
import com.br.yat.gerenciador.controller.MenuPrincipalController;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;

public final class ViewFactory {
	
	public static EmpresaView createEmpresaView(String tipoCadastro) {
		EmpresaView view = new EmpresaView();
		EmpresaService service = new EmpresaService();
		
		var ePrincipal = new DadoPrincipalController(view.getDadoPrincipal(),service);
		var eEndereco =new DadoEnderecoController(view.getDadoEndereco(),service);
		var eContato =new DadoContatoController(view.getDadoContato(),service);
		var eFiscal =new DadoFiscalController(view.getDadoFiscal(),service);
		var eRepresentante =new DadoRepresentanteController(view.getDadoRepresentante(),service);
		var eBancario =new DadoBancarioController(view.getDadoBancario(),service);
		var eComplementar =new DadoComplementarController(view.getDadoComplementar(),service);
		
		new EmpresaController(view,service,ePrincipal,eEndereco,eContato,eFiscal,eRepresentante,eBancario,eComplementar,tipoCadastro);
		return view;
	}
	
	public static MenuPrincipal createMenuPrincipal() {
		MenuPrincipal view = new MenuPrincipal();
		
		new MenuPrincipalController(view);
		
		return view;
	}
	
}
