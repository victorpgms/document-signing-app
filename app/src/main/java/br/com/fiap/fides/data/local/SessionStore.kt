package br.com.fiap.fides.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.AuthenticatedUser
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.repository.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "fides_session")

class SessionStore(private val context: Context) : SessionRepository {
    private object Keys {
        val userId = stringPreferencesKey("user_id")
        val email = stringPreferencesKey("email")
        val displayName = stringPreferencesKey("display_name")
        val role = stringPreferencesKey("role")
    }

    override val session: Flow<UserSession?> = context.sessionDataStore.data.map { preferences ->
        val id = preferences[Keys.userId] ?: return@map null
        val email = preferences[Keys.email] ?: return@map null
        val displayName = preferences[Keys.displayName] ?: return@map null
        val role = preferences[Keys.role]?.let { value ->
            runCatching { UserRole.valueOf(value) }.getOrNull()
        } ?: return@map null
        UserSession(id, email, displayName, role)
    }

    override suspend fun save(user: AuthenticatedUser) {
        context.sessionDataStore.edit { preferences ->
            preferences[Keys.userId] = user.id
            preferences[Keys.email] = user.email
            preferences[Keys.displayName] = user.displayName
            preferences[Keys.role] = user.role.name
        }
    }

    override suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
