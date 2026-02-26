package com.br.yat.gerenciador.dao;

import java.sql.Connection;

import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;

public interface DaoFactory {
	UsuarioDao createUsuarioDao(Connection conn);

	PermissaoDao createPermissaoDao(Connection conn);

	UsuarioPermissaoDao createUsuarioPermissaoDao(Connection conn);

	EmpresaDao createEmpresaDao(Connection conn);

	LogSistemaDao createLogSistemaDao(Connection conn);
	
	PerfilDao createPerfilDao(Connection conn);
	
	PerfilPermissoesDao createPerfilPermissoesDao(Connection conn);
	
	MenuSistemaDao createMenuSistemaDao(Connection conn);
	
	PermissaoMenuDao createPermissaoMenuDao(Connection conn);

}
