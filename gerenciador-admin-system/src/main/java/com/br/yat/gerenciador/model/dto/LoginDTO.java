package com.br.yat.gerenciador.model.dto;

import java.util.List;

import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;

public record LoginDTO(Usuario user, List<MenuChave> permissoes) {

}
