package br.com.fiap.fides.domain.security

import br.com.fiap.fides.domain.model.UserRole

object AccessPolicy {
    fun canManageSigners(role: UserRole): Boolean = role == UserRole.ADMIN

    fun canCreateDocuments(role: UserRole): Boolean = role == UserRole.ADMIN

    fun canSignDocuments(role: UserRole): Boolean = role == UserRole.SIGNER
}
