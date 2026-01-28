package com.br.yat.gerenciador.model.dto;

import java.util.List;

import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Representante;

public record EmpresaDTO( Empresa empresa, 
		List<Contato> contatos,
		List<Representante> representantes,
		List<Banco> bancos,
		Complementar complementar,
		List<Documento> documentos) {

}
