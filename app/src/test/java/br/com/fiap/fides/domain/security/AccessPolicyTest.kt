package br.com.fiap.fides.domain.security

import br.com.fiap.fides.domain.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPolicyTest {
    @Test
    fun `only admin can manage signers and create documents`() {
        assertTrue(AccessPolicy.canManageSigners(UserRole.ADMIN))
        assertTrue(AccessPolicy.canCreateDocuments(UserRole.ADMIN))
        assertFalse(AccessPolicy.canManageSigners(UserRole.SIGNER))
        assertFalse(AccessPolicy.canCreateDocuments(UserRole.SIGNER))
    }

    @Test
    fun `only signer can sign documents`() {
        assertTrue(AccessPolicy.canSignDocuments(UserRole.SIGNER))
        assertFalse(AccessPolicy.canSignDocuments(UserRole.ADMIN))
    }
}
