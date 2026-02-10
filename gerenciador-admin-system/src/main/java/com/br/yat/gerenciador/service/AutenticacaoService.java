package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.TimeUtils;

public class AutenticacaoService extends BaseService {

    public Usuario autenticar(String email, char[] senhaPura) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDao dao = new UsuarioDao(conn);
            LogSistemaDao logDao = new LogSistemaDao(conn);
            ParametroSistemaService parametroService = new ParametroSistemaService();

            int maxTentativas = parametroService.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5);
            int minutosBloqueio = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);

            Usuario user = dao.buscarPorEmail(email);

            // 1. Validações de Existência e Status
            if (user == null) {
                logDao.save(AuditLogHelper.gerarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario", "Inexistente: " + email));
                throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO OU SENHA INVÁLIDOS.");
            }

            if (StatusUsuario.BLOQUEADO == user.getStatus()) {
                throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "CONTA BLOQUEADA PERMANENTEMENTE.");
            }

            if (StatusUsuario.INATIVO == user.getStatus()) {
                throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ESTE USUÁRIO ESTÁ INATIVO.");
            }
            // 2. Bloqueio Temporário
            if (user.getBloqueadoAte() != null) {
                if (user.getBloqueadoAte().isAfter(LocalDateTime.now())) {
                    throw new ValidationException(ValidationErrorType.ACCESS_DENIED, 
                        "ACESSO SUSPENSO ATÉ " + TimeUtils.formatarDataHora(user.getBloqueadoAte()));
                } else {
                    dao.resetTentativasFalhas(user.getIdUsuario());
                }
            }

            // 3. Verificação de Senha
            boolean senhaValida = PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString());

            if (!senhaValida) {
                processarFalhaLogin(dao, logDao, user, email, maxTentativas, minutosBloqueio);
            }
            if (user.getSenhaExpiraEm() != null && LocalDateTime.now().isAfter(user.getSenhaExpiraEm())) {
                user.setForcarResetSenha(true); 
            }

            // 4. Sucesso
            dao.atualizarUltimoLogin(user.getIdUsuario());
            dao.resetTentativasFalhas(user.getIdUsuario());
            logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "LOGIN_SUCESSO", "usuario", user.getIdUsuario(), null, "Sessão Iniciada"));

            return user;

        } catch (SQLException e) {
            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "Erro de conexão.", e);
        } finally {
            SensitiveData.safeClear(senhaPura);
        }
    }

    private void processarFalhaLogin(UsuarioDao dao, LogSistemaDao logDao, Usuario user, String email, int max, int min) throws SQLException {
        if (!user.isMaster()) {
            int tentativasAtuais = dao.incrementarERetornarTentativas(email);
            if (tentativasAtuais >= max) {
                LocalDateTime ate = LocalDateTime.now().plusMinutes(min);
                dao.bloquearTemporariamente(user.getIdUsuario(), ate);
                logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario", user.getIdUsuario(), "Tentativas: " + tentativasAtuais, "Até: " + ate));
                throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "LIMITE ATINGIDO. SUSPENSO ATÉ " + TimeUtils.formatarDataHora(ate));
            }
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA. TENTATIVA " + tentativasAtuais + " DE " + max + ".");
        }
        throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA.");
    }
    
    
 // Requisitos para os métodos abaixo
    private static final String ESPECIAIS = "!@#$%^&*(),.?\":{}|<>";
    private static final java.util.EnumSet<com.br.yat.gerenciador.model.enums.RegraSenha> REGRAS_OBRIGATORIAS = 
        java.util.EnumSet.of(com.br.yat.gerenciador.model.enums.RegraSenha.MAIUSCULA, 
                             com.br.yat.gerenciador.model.enums.RegraSenha.NUMERO,
                             com.br.yat.gerenciador.model.enums.RegraSenha.ESPECIAL);

    public void validarComplexidade(char[] senha) {
        ParametroSistemaService parametroService = new ParametroSistemaService();
        int minTamanho = parametroService.getInt(ParametroChave.SENHA_MIN_TAMANHO, 6);
        
        if (senha == null || senha.length < minTamanho) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A SENHA DEVE TER NO MÍNIMO " + minTamanho + " CARACTERES.");
        }

        java.util.EnumSet<com.br.yat.gerenciador.model.enums.RegraSenha> regras = java.util.EnumSet.noneOf(com.br.yat.gerenciador.model.enums.RegraSenha.class);

        for (char c : senha) {
            if (Character.isUpperCase(c)) regras.add(com.br.yat.gerenciador.model.enums.RegraSenha.MAIUSCULA);
            if (Character.isDigit(c)) regras.add(com.br.yat.gerenciador.model.enums.RegraSenha.NUMERO);
            if (ESPECIAIS.indexOf(c) >= 0) regras.add(com.br.yat.gerenciador.model.enums.RegraSenha.ESPECIAL);
        }

        if (!regras.containsAll(REGRAS_OBRIGATORIAS)) {
            StringBuilder msg = new StringBuilder("A SENHA DEVE CONTER: ");
            if (!regras.contains(com.br.yat.gerenciador.model.enums.RegraSenha.MAIUSCULA)) msg.append("UMA LETRA MAIÚSCULA, ");
            if (!regras.contains(com.br.yat.gerenciador.model.enums.RegraSenha.NUMERO)) msg.append("UM NÚMERO, ");
            if (!regras.contains(com.br.yat.gerenciador.model.enums.RegraSenha.ESPECIAL)) msg.append("UM CARACTERE ESPECIAL.");
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, msg.toString().replace(", .", "."));
        }
    }

    public String gerarHashSeguro(char[] senhaPura) {
        validarComplexidade(senhaPura);
        return PasswordUtils.hashPassword(senhaPura);
    }
    
    /**
     * Reseta a senha de um usuário para um valor padrão.
     * Operação exclusiva para usuários privilegiados (Policy).
     */
    public String resetarSenha(int idUsuarioAlvo, Usuario executor) {
        // 1. Double Validation via Policy: Segurança em nível de serviço
        if (!UsuarioPolicy.isPrivilegiado(executor)) {
            throw new ValidationException(ValidationErrorType.ACCESS_DENIED, 
                "APENAS UM USUÁRIO MASTER PODE RESETAR SENHAS.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDao dao = new UsuarioDao(conn);
            ParametroSistemaService parametroService = new ParametroSistemaService();

            // 2. Validação de existência do alvo
            Usuario alvo = dao.searchById(idUsuarioAlvo);
            if (alvo == null) {
                throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "USUÁRIO NÃO ENCONTRADO.");
            }

            // 3. Busca dinâmica da senha padrão (Fallback para "Mudar@123" se não configurado)
            String senhaPadrao = parametroService.getString(ParametroChave.SENHA_RESET_PADRAO, "Mudar@123");
            char[] senhaChars = senhaPadrao.toCharArray();
            
            try {
                // A própria geração do hash já valida a complexidade contra os parâmetros do sistema
                String hash = gerarHashSeguro(senhaChars);
                
                ConnectionFactory.beginTransaction(conn);
                try {
                    // Atualiza o hash e seta 'forcar_reset_senha = 1' no banco (conforme o método do DAO)
                    dao.atualizarSenha(idUsuarioAlvo, hash);
                    
                    // Limpa bloqueios por tentativas falhas
                    dao.resetTentativasFalhas(idUsuarioAlvo);
                    
                    new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso(
                        "SEGURANCA", "RESET_SENHA", "usuario", idUsuarioAlvo, 
                        "Executor: " + executor.getNome(), 
                        "Senha resetada para o padrão definido nos parâmetros do sistema."
                    ));
                    
                    ConnectionFactory.commitTransaction(conn);
                    return senhaPadrao;
                } catch (Exception e) {
                    ConnectionFactory.rollbackTransaction(conn);
                    throw e;
                }
            } finally {
                // Limpeza crucial de dados sensíveis da memória RAM
                SensitiveData.safeClear(senhaChars);
            }
        } catch (SQLException e) {
            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO DE INFRAESTRUTURA AO RESETAR SENHA", e);
        }
    }
    
    public void alterarSenhaObrigatoria(int idUsuario, char[] novaSenha, char[] confirmacao) {
        // 1. Validação de UI/Front (Igualdade)
        if (!java.util.Arrays.equals(novaSenha, confirmacao)) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "AS SENHAS DIGITADAS NÃO CONFEREM.");
        }

        // 2. Double Validation (Complexidade vinda do Banco)
        validarComplexidade(novaSenha); 

        try (Connection conn = ConnectionFactory.getConnection()) {
            UsuarioDao dao = new UsuarioDao(conn);
            ParametroSistemaService parametroService = new ParametroSistemaService();

            // 3. Cálculo da expiração baseado nos parâmetros
            int diasValidade = parametroService.getInt(ParametroChave.FORCAR_TROCA_SENHA_DIAS, 90);
            LocalDateTime expiraEm = LocalDateTime.now().plusDays(diasValidade);

            String hash = PasswordUtils.hashPassword(novaSenha);

            ConnectionFactory.beginTransaction(conn);
            try {
                dao.atualizarSenhaAposReset(idUsuario, hash, expiraEm);
                
                new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso(
                    "SEGURANCA", "TROCA_SENHA_OBRIGATORIA", "usuario", idUsuario, 
                    null, "Usuário cumpriu a troca obrigatória com sucesso."
                ));
                
                ConnectionFactory.commitTransaction(conn);
            } catch (Exception e) {
                ConnectionFactory.rollbackTransaction(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO SALVAR NOVA SENHA", e);
        } finally {
            SensitiveData.safeClear(novaSenha);
            SensitiveData.safeClear(confirmacao);
        }
    }
}