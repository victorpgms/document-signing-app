package br.com.fiap.fides.domain.security

class AccessDeniedException(message: String = "Usuário não autorizado para esta operação.") : IllegalStateException(message)
