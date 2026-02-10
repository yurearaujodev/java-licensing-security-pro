package com.br.yat.gerenciador.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SensitiveDataExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
	    String name = f.getName().toLowerCase();
	    
	    // Filtra por nomes de campos que representam perigo
	    return name.contains("senha") 
	           || name.contains("password")
	           || name.contains("hash")
	           || name.contains("salt")
	           || name.contains("token");
	}

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}