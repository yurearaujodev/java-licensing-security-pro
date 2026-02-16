package com.br.yat.gerenciador.model.enums;

import com.br.yat.gerenciador.util.MenuRegistry;

/**
 * Enumeração que representa as chaves de menus da aplicação.
 * <p>
 * Cada constante corresponde a uma funcionalidade específica organizada por
 * módulos:
 * <ul>
 * <li>Dashboard</li>
 * <li>Cadastros</li>
 * <li>Consultas</li>
 * <li>Licenças</li>
 * <li>Financeiro</li>
 * <li>Relatórios</li>
 * <li>Auditoria</li>
 * <li>Configuração</li>
 * <li>Ajuda</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Utilizada para registro e controle de menus em {@link MenuRegistry} e
 * mapeamento de permissões em {@link PermissionMenuMapper}.
 * </p>
 */
public enum MenuChave {

	// === DASHBOARD (Nível 1) ===
	DASHBOARD_GERAL("DASHBOARD", "Visão geral dos indicadores", 1),
	DASHBOARD_ALERTAS("DASHBOARD", "Alertas e notificações do sistema", 1),
	DASHBOARD_LICENCAS_A_VENCER("DASHBOARD", "Painel de licenças próximas ao vencimento", 1),
	DASHBOARD_LICENCAS_BLOQUEADAS("DASHBOARD", "Painel de licenças suspensas", 1),
	DASHBOARD_PAGAMENTOS_PENDENTES("DASHBOARD", "Painel de pendências financeiras", 1),

	// === CADASTROS (Nível 2) ===
	CADASTROS_EMPRESA_CLIENTE("CADASTROS", "Gestão de empresas clientes", 2),
	//CADASTROS_PERFIL("CADASTROS", "Gestão de perfis de acesso", 2),
//	CADASTROS_USUARIO("CADASTROS", "Gestão de usuários do sistema", 2),
	CADASTROS_PLANO_DE_LICENCA("CADASTROS", "Configuração de planos de serviço", 2),

	// === CONSULTAS (Nível 2) ===
	CONSULTAS_EMPRESAS_CLIENTES("CONSULTAS", "Consulta rápida de empresas", 2),
//	CONSULTAS_USUARIOS("CONSULTAS", "Consulta de usuários cadastrados", 2),
//	CONSULTAS_PERFIL("CONSULTAS", "Consulta de perfis existentes", 2),
	CONSULTAS_PLANO_DE_LICENCA("CONSULTAS", "Consulta de planos disponíveis", 2),
	CONSULTAS_LICENCAS("CONSULTAS", "Consulta global de licenças", 2),
	CONSULTAS_DISPOSITIVOS("CONSULTAS", "Consulta de dispositivos ativos", 2),
	CONSULTAS_PAGAMENTOS("CONSULTAS", "Consulta de histórico de pagamentos", 2),

	// === LICENÇAS (Nível 3) ===
	LICENCAS_GERAR_LICENCA("LICENÇAS", "Emissão de novas licenças", 3),
	LICENCAS_RENOVAR_LICENCA("LICENÇAS", "Renovação de licenças existentes", 3),
	LICENCAS_ATIVAR_BLOQUEAR_LICENCA("LICENÇAS", "Controle de status de licença", 3),
	LICENCAS_DISPOSITIVOS_VINCULADOS("LICENÇAS", "Gestão de dispositivos por licença", 3),
	LICENCAS_HISTORICO_DA_LICENCA("LICENÇAS", "Logs de eventos da licença", 3),

	// === FINANCEIRO (Nível 3) ===
	FINANCEIRO_REGISTRAR_PAGAMENTO("FINANCEIRO", "Lançamento de recebimentos", 3),
	FINANCEIRO_CONSULTAR_PAGAMENTOS("FINANCEIRO", "Gestão financeira de clientes", 3),
	FINANCEIRO_FATURAMENTO_RECEITA("FINANCEIRO", "Controle de faturamento bruto", 3),
	FINANCEIRO_RELATORIOS("FINANCEIRO", "Relatórios financeiros detalhados", 3),

	// === RELATÓRIOS GERENCIAIS (Nível 4) ===
	RELATORIOS_GERENCIAIS_LICENCAS_ATIVAS_EXPERIDAS("RELATÓRIOS", "Relatório de licenças ativas/expiradas", 4),
	RELATORIOS_GERENCIAIS_LICENCAS_A_VENCER("RELATÓRIOS", "Relatório de previsibilidade de vencimento", 4),
	RELATORIOS_GERENCIAIS_RECEITA_POR_PERIODO("RELATÓRIOS", "Análise de receita por data", 4),
	RELATORIOS_GERENCIAIS_RECEITA_POR_PLANO("RELATÓRIOS", "Análise de receita por tipo de plano", 4),
	RELATORIOS_GERENCIAIS_EMPRESAS_POR_PLANO("RELATÓRIOS", "Distribuição de clientes por plano", 4),
	RELATORIOS_GERENCIAIS_DISPOSITIVOS_POR_LICENCA("RELATÓRIOS", "Relatório de uso de hardware", 4),
	RELATORIOS_GERENCIAIS_USUARIOS_POR_EMPRESA("RELATÓRIOS", "Lista de usuários por cliente", 4),

	// === AUDITORIA (Nível 4) ===
	AUDITORIA_LOG_DO_SISTEMA("AUDITORIA", "Logs de operações e erros", 4),
	AUDITORIA_HISTORICO_DE_LICENCAS("AUDITORIA", "Rastreabilidade de licenças", 4),

	// === CONFIGURAÇÃO (Nível 5) ===
	CONFIGURACAO_EMPRESA_FORNECEDORA("CONFIGURAÇÃO", "Dados da empresa proprietária", 5),
	CONFIGURACAO_PREFERENCIAS_DO_SISTEMA("CONFIGURAÇÃO", "Personalização visual e preferências do usuário", 5),
	CONFIGURACAO_PARAMETRO_SISTEMA("CONFIGURAÇÃO", "Parâmetros globais do sistema", 5),
	CONFIGURACAO_PARAMETRO_LICENCA("CONFIGURAÇÃO", "Regras de negócio das licenças", 5),
	CONFIGURACAO_SEGURANCA("CONFIGURAÇÃO", "Políticas de senha e acesso", 5),
	CONFIGURACAO_PERMISSAO("CONFIGURAÇÃO", "Gestão de matriz de permissões", 5),
	CONFIGURACAO_USUARIOS_PERMISSOES("CONFIGURAÇÃO", "Atribuição avançada de acessos", 5),
	CONFIGURACAO_MANUTENCAO("CONFIGURAÇÃO", "Ferramentas de manutenção", 5),
	CONFIGURACAO_CONEXAO_BANCO_DADOS("CONFIGURAÇÃO", "Configurações de infraestrutura", 5),
	CONFIGURACAO_BACKUP_DE_DADOS("CONFIGURAÇÃO", "Rotinas de backup", 5),
	CONFIGURACAO_RESTAURAR_BACKUP("CONFIGURAÇÃO", "Recuperação de desastres", 5),
	CONFIGURACAO_LIMPEZA_DE_LOGS("CONFIGURAÇÃO", "Expurgo de dados antigos", 5),
	CONFIGURACAO_VERSAO_DO_SISTEMA("CONFIGURAÇÃO", "Informações de build e versão", 5),

	// === AJUDA (Nível 1) ===
	AJUDA_MANUAL_DO_SISTEMA("AJUDA", "Documentação do usuário", 1),
	AJUDA_SUPORTE_TECNICO("AJUDA", "Canais de atendimento", 1),
	AJUDA_SOBRE_O_SISTEMA("AJUDA", "Informações de copyright e licença", 1),
	AJUDA_VERIFICAR_ATUALIZACOES("AJUDA", "Check de updates", 1);

	private final String categoria;
	private final String descricao;
	private final int nivel;

	MenuChave(String categoria, String descricao, int nivel) {
		this.categoria = categoria;
		this.descricao = descricao;
		this.nivel = nivel;
	}

	public String getCategoria() {
		return categoria;
	}

	public String getDescricao() {
		return descricao;
	}

	public int getNivel() {
		return nivel;
	}
}
