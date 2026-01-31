package com.br.yat.gerenciador.service;

import com.br.yat.gerenciador.config.ConnectionFactory;
import com.br.yat.gerenciador.model.dto.DatabaseConfigDTO;
import com.br.yat.gerenciador.config.DatabaseStatus;

public class DatabaseConnectionService {

    private final DatabaseSetupService setupService;

    public DatabaseConnectionService(DatabaseSetupService setupService) {
        this.setupService = setupService;
    }

    public DatabaseStatus testarConfiguracao(DatabaseConfigDTO dto) {

        setupService.saveDatabaseConfigConfiguration(dto);

    
        ConnectionFactory.shutdown();
        return ConnectionFactory.reloadAndCheck();
    }
}