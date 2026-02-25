package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
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
}

