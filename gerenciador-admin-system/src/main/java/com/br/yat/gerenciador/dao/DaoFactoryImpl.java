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

public class DaoFactoryImpl implements DaoFactory {

    @Override
    public UsuarioDao createUsuarioDao(Connection conn) {
        return new UsuarioDao(conn);
    }

    @Override
    public PermissaoDao createPermissaoDao(Connection conn) {
        return new PermissaoDao(conn);
    }

    @Override
    public UsuarioPermissaoDao createUsuarioPermissaoDao(Connection conn) {
        return new UsuarioPermissaoDao(conn);
    }

    @Override
    public EmpresaDao createEmpresaDao(Connection conn) {
        return new EmpresaDao(conn);
    }

    @Override
    public LogSistemaDao createLogSistemaDao(Connection conn) {
        return new LogSistemaDao(conn);
    }

	@Override
	public PerfilDao createPerfilDao(Connection conn) {
		return new PerfilDao(conn);
	}

	@Override
	public PerfilPermissoesDao createPerfilPermissoesDao(Connection conn) {
		return new PerfilPermissoesDao(conn);
	}

	@Override
	public MenuSistemaDao createMenuSistemaDao(Connection conn) {
		return new MenuSistemaDao(conn);
	}

	@Override
	public PermissaoMenuDao createPermissaoMenuDao(Connection conn) {
		return new PermissaoMenuDao(conn);
	}
}

