package com.br.yat.gerenciador.model;

import java.util.List;

public record EmpresaDTO( Empresa empresa, 
		List<Contato> contatos,
		List<Representante> representantes,
		List<Banco> bancos,
		Complementar complementar,
		List<Documento> documentos) {

}
