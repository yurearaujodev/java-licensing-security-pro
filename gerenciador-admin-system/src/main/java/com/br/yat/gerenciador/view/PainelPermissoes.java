package com.br.yat.gerenciador.view;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

import net.miginfocom.swing.MigLayout;

public class PainelPermissoes extends JPanel {

	private static final long serialVersionUID = 1L;
	private final Map<String, JCheckBox> chkTodosPorCategoria = new LinkedHashMap<>();
	private final Map<MenuChave, Map<TipoPermissao, JCheckBox>> permissoesGranulares = new LinkedHashMap<>();
	private final Map<MenuChave, DateTimePicker> datasExpiracao = new LinkedHashMap<>();
	private final Map<String, List<MenuChave>> gruposPermissoes = new LinkedHashMap<>();
	private final boolean mostrarExpiracao;
	private boolean painelHabilitado = true;

	public PainelPermissoes(boolean mostrarExpiracao) {
		this.mostrarExpiracao = mostrarExpiracao;
		setLayout(new MigLayout("wrap 1, fillx", "[grow]", "[]10[]"));
		setBorder(BorderFactory.createTitledBorder("Permissões"));
	}

	public void construirGrade(Map<String, List<MenuChave>> grupos) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("PainelPermissoes deve ser usado na EDT.");
		}
		limpar();
		if (grupos == null || grupos.isEmpty())
			return;

		grupos.forEach((k, v) -> gruposPermissoes.put(k, List.copyOf(v)));

		TipoPermissao[] tipos = TipoPermissao.values();

		grupos.forEach((categoria, chaves) -> {
			// Definição dinâmica das colunas:
			// Descrição (grow) + 3 tipos (50!) + Opcional Data (130!)
			StringBuilder colunas = new StringBuilder("[grow,fill]");
			for (int i = 0; i < tipos.length; i++)
				colunas.append("[50!]");
			if (mostrarExpiracao)
				colunas.append("[220!]");

			JPanel pnlCat = new JPanel(new MigLayout("fillx, insets 10", colunas.toString(), "[]5[]"));
			pnlCat.setBorder(BorderFactory.createTitledBorder(categoria));

			// Checkbox "Marcar Todos" da Categoria
			JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS DE: " + categoria);
			chkTodosPorCategoria.put(categoria, chkTodos);
			int totalColunasHeader = 1 + tipos.length + (mostrarExpiracao ? 1 : 0);
			pnlCat.add(chkTodos, "span " + totalColunasHeader + ", left, gapy 5 10, wrap");

			// Cabeçalho da Tabela Interna
			pnlCat.add(LabelFactory.createLabel("FUNCIONALIDADE / DESCRIÇÃO"), "center");
			for (TipoPermissao tipo : tipos) {
				pnlCat.add(LabelFactory.createLabel(tipo.name()), "center");
			}
			if (mostrarExpiracao) {
				pnlCat.add(LabelFactory.createLabel("EXPIRA EM"), "center, wrap");
			} else {
				pnlCat.add(new JLabel(), "wrap");
			}

			// Linhas de Permissão
			for (MenuChave chave : chaves) {
				String labelHtml = "<html><b>" + chave.name().replace("_", " ") + "</b><br>"
						+ "<font color='#666666' size='2'>" + chave.getDescricao() + "</font></html>";
				pnlCat.add(LabelFactory.createLabel(labelHtml));

				Map<TipoPermissao, JCheckBox> tiposMap = new LinkedHashMap<>();
				for (TipoPermissao tipo : tipos) {
					JCheckBox chk = new JCheckBox();
					pnlCat.add(chk, "center");
					tiposMap.put(tipo, chk);
				}
				permissoesGranulares.put(chave, tiposMap);

				// Campo de Data (Apenas se solicitado no construtor)
				if (mostrarExpiracao) {
					DateTimePicker dtPicker = criarConfigurarDateTimePicker();
					datasExpiracao.put(chave, dtPicker);
					pnlCat.add(dtPicker, "growx, h 26!, wrap");
				} else {
					pnlCat.add(new JLabel(), "wrap");
				}
			}
			this.add(pnlCat, "growx, wrap 10");
		});

		revalidate();
		repaint();
	}

	private DateTimePicker criarConfigurarDateTimePicker() {
		// Configurações de Data
		DatePickerSettings dateSettings = new DatePickerSettings();
		dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
		dateSettings.setAllowKeyboardEditing(false);
		dateSettings.setAllowEmptyDates(true);

		// Configurações de Hora
		TimePickerSettings timeSettings = new TimePickerSettings();
		timeSettings.setFormatForDisplayTime("HH:mm");
		timeSettings.setFormatForMenuTimes("HH:mm");
		timeSettings.setAllowKeyboardEditing(false);
		timeSettings.setAllowEmptyTimes(true);

		DateTimePicker dtPicker = new DateTimePicker(dateSettings, timeSettings);

		// Estilização do Botão conforme seu pedido
		JButton btnCalendar = dtPicker.getDatePicker().getComponentToggleCalendarButton();
		btnCalendar.setText("");
		btnCalendar.setIcon(IconFactory.dataHora());
		btnCalendar.setBorderPainted(false);
		btnCalendar.setContentAreaFilled(false);

		return dtPicker;
	}

	public void limpar() {
		removeAll();
		chkTodosPorCategoria.clear();
		permissoesGranulares.clear();
		datasExpiracao.clear();
		gruposPermissoes.clear();
		painelHabilitado = true;
	}

	public void resetarSelecoes() {
		permissoesGranulares.values().forEach(mapa -> mapa.values().forEach(chk -> chk.setSelected(false)));
		datasExpiracao.values().forEach(picker -> picker.setDateTimeStrict(null));
		chkTodosPorCategoria.values().forEach(chk -> chk.setSelected(false));
	}

	// --- Getters e Setters de Alta Performance ---

	public void setPermissao(MenuChave chave, TipoPermissao tipo, boolean valor) {
		Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
		if (mapa != null) {
			JCheckBox chk = mapa.get(tipo);
			if (chk != null) {
				chk.setSelected(valor);
			}
		}

	}

	public boolean isPermissaoSelecionada(MenuChave chave, TipoPermissao tipo) {
		Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
		if (mapa == null)
			return false;
		JCheckBox chk = mapa.get(tipo);
		return chk != null && chk.isSelected();

	}

	public String getDataExpiracao(MenuChave chave) {
		DateTimePicker picker = datasExpiracao.get(chave);
		// Retorna no formato String esperado pela sua Service ou null
		return (picker != null && picker.getDateTimePermissive() != null)
				? picker.getDateTimeStrict().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
				: "";
	}

	public void setDataExpiracao(MenuChave chave, java.time.LocalDateTime data) {
		DateTimePicker picker = datasExpiracao.get(chave);
		if (picker != null) {
			// Seta o LocalDateTime direto, sem conversão de String
			picker.setDateTimeStrict(data);
		}
	}

	public void setHabilitado(boolean habilitado) {
		this.painelHabilitado = habilitado;

		permissoesGranulares.values().forEach(mapa -> mapa.values().forEach(chk -> chk.setEnabled(habilitado)));

		datasExpiracao.values().forEach(txt -> txt.setEnabled(habilitado));
		chkTodosPorCategoria.values().forEach(chk -> chk.setEnabled(habilitado));
	}

	public Set<MenuChave> getChavesConstruidas() {
		return new LinkedHashSet<>(permissoesGranulares.keySet());
	}

	public List<MenuChave> getChavesDaCategoria(String categoria) {
		return gruposPermissoes.getOrDefault(categoria, List.of());
	}

	public Set<String> getCategorias() {
		return new LinkedHashSet<>(chkTodosPorCategoria.keySet());
	}

	public void aplicarRestricaoPermissao(MenuChave chave, TipoPermissao tipo, boolean permitido) {
		Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
		if (mapa == null)
			return;

		JCheckBox chk = mapa.get(tipo);
		if (chk == null)
			return;

		// Respeita o estado global do painel
		chk.setEnabled(permitido && painelHabilitado);

		if (!permitido) {
			chk.setSelected(false);
			chk.setToolTipText("Você não possui essa permissão.");
		} else {
			chk.setToolTipText(null);
		}
	}

	public void marcarCategoria(String categoria, boolean marcar) {
		List<MenuChave> chaves = gruposPermissoes.get(categoria);
		if (chaves == null)
			return;

		for (MenuChave chave : chaves) {
			Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
			if (mapa == null)
				continue;

			for (Map.Entry<TipoPermissao, JCheckBox> entry : mapa.entrySet()) {
				JCheckBox chk = entry.getValue();
				if (chk.isEnabled()) {
					chk.setSelected(marcar);
				}
			}
		}
	}

	public boolean isCategoriaTotalmenteMarcada(String categoria) {
		List<MenuChave> chaves = gruposPermissoes.get(categoria);
		if (chaves == null)
			return false;

		for (MenuChave chave : chaves) {
			Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
			if (mapa == null)
				continue;

			for (JCheckBox chk : mapa.values()) {
				if (chk.isEnabled() && !chk.isSelected()) {
					return false;
				}
			}
		}
		return true;
	}

	public void addListenerCategoria(String categoria, Consumer<Boolean> listener) {
		JCheckBox chk = chkTodosPorCategoria.get(categoria);
		if (chk != null) {
			chk.addActionListener(e -> listener.accept(chk.isSelected()));
		}
	}

	public void setCategoriaMarcada(String categoria, boolean marcada) {
		JCheckBox chk = chkTodosPorCategoria.get(categoria);
		if (chk != null)
			chk.setSelected(marcada);
	}
}