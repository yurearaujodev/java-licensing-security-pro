package com.br.yat.gerenciador.service;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.configurations.DatabaseStatus;
import com.br.yat.gerenciador.model.dto.DatabaseConfigDTO;

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