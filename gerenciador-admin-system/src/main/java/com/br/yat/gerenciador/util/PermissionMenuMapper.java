package com.br.yat.gerenciador.util;

import java.util.List;
import java.util.Map;

import com.br.yat.gerenciador.model.enums.MenuChave;
/**
 * Classe utilitária para mapeamento de permissões em menus da aplicação.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Associar permissões (String) a listas de menus ({@link MenuChave}).</li>
 * <li>Retornar menus correspondentes a uma permissão.</li>
 * <li>Interpretar permissões como chaves individuais de menu quando aplicável.</li>
 * </ul>
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */
public final class PermissionMenuMapper {

	private static final Map<String, List<MenuChave>> MAP = Map.of(

			"DASHBOARD",
			List.of(MenuChave.DASHBOARD_GERAL, MenuChave.DASHBOARD_ALERTAS, MenuChave.DASHBOARD_LICENCAS_A_VENCER,
					MenuChave.DASHBOARD_LICENCAS_BLOQUEADAS, MenuChave.DASHBOARD_PAGAMENTOS_PENDENTES),
			"CADASTRO",
			List.of(MenuChave.CADASTROS_EMPRESA_CLIENTE, MenuChave.CADASTROS_USUARIO,
					MenuChave.CADASTROS_PLANO_DE_LICENCA),
			"CONSULTA",
			List.of(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, MenuChave.CONSULTAS_USUARIOS,
					MenuChave.CONSULTAS_PLANO_DE_LICENCA, MenuChave.CONSULTAS_DISPOSITIVOS,
					MenuChave.CONSULTAS_PAGAMENTOS),
			"LICENCA",
			List.of(MenuChave.LICENCAS_GERAR_LICENCA, MenuChave.LICENCAS_RENOVAR_LICENCA, MenuChave.CONSULTAS_LICENCAS,
					MenuChave.LICENCAS_ATIVAR_BLOQUEAR_LICENCA, MenuChave.LICENCAS_DISPOSITIVOS_VINCULADOS,
					MenuChave.LICENCAS_HISTORICO_DA_LICENCA),
			"FINANCEIRO",
			List.of(MenuChave.FINANCEIRO_REGISTRAR_PAGAMENTO, MenuChave.FINANCEIRO_CONSULTAR_PAGAMENTOS,
					MenuChave.FINANCEIRO_FATURAMENTO_RECEITA, MenuChave.FINANCEIRO_RELATORIOS),
			"RELATORIOS",
			List.of(MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_ATIVAS_EXPERIDAS,
					MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_A_VENCER,
					MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PERIODO,
					MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PLANO,
					MenuChave.RELATORIOS_GERENCIAIS_EMPRESAS_POR_PLANO,
					MenuChave.RELATORIOS_GERENCIAIS_DISPOSITIVOS_POR_LICENCA,
					MenuChave.RELATORIOS_GERENCIAIS_USUARIOS_POR_EMPRESA),
			"AUDITORIA", List.of(MenuChave.AUDITORIA_LOG_DO_SISTEMA, MenuChave.AUDITORIA_HISTORICO_DE_LICENCAS),
			"CONFIGURACAO",
			List.of(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA,
					MenuChave.CONFIGURACAO_PARAMETRO_LICENCA, MenuChave.CONFIGURACAO_SEGURANCA,
					MenuChave.CONFIGURACAO_PERMISSAO, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES,
					MenuChave.CONFIGURACAO_MANUTENCAO, MenuChave.CONFIGURACAO_BACKUP_DE_DADOS,
					MenuChave.CONFIGURACAO_RESTAURAR_BACKUP, MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS,
					MenuChave.CONFIGURACAO_VERSAO_DO_SISTEMA),
			"AJUDA", List.of(MenuChave.AJUDA_MANUAL_DO_SISTEMA, MenuChave.AJUDA_SUPORTE_TECNICO,
					MenuChave.AJUDA_SOBRE_O_SISTEMA, MenuChave.AJUDA_VERIFICAR_ATUALIZACOES));

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private PermissionMenuMapper() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Retorna a lista de menus associados a uma permissão.
	 * <p>
	 * Se a permissão for nula, vazia ou inválida, retorna lista vazia.
	 * Se a permissão corresponder a uma chave no mapa, retorna a lista associada.
	 * Caso contrário, tenta interpretar a permissão como um {@link MenuChave} individual.
	 * </p>
	 * 
	 * @param permissao nome da permissão
	 * @return lista de menus associados ou vazia se inválida
	 */
	public static List<MenuChave> getMenus(String permissao) {
		if (permissao == null || permissao.isBlank()) {
			return List.of();
		}

		if (MAP.containsKey(permissao)) {
			return MAP.get(permissao);
		}

		try {
			return List.of(MenuChave.valueOf(permissao));
		} catch (IllegalArgumentException e) {
			return List.of();
		}
	}
}
