package com.anmei.warehouseputaway.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {

        private val OPERATOR_NAME =
            stringPreferencesKey(
                "operator_name"
            )
    }

    /**
     * Observe saved operator name.
     */
    val operatorName: Flow<String> =
        context.userPreferencesDataStore.data
            .map { preferences ->

                preferences[
                    OPERATOR_NAME
                ] ?: ""
            }

    /**
     * Save operator name.
     */
    suspend fun saveOperatorName(
        name: String
    ) {

        context.userPreferencesDataStore.edit { preferences ->

            preferences[
                OPERATOR_NAME
            ] =
                name.trim()
        }
    }

    /**
     * Read the current operator name once.
     *
     * first() is important here.
     *
     * Do NOT use collect().
     */
    suspend fun getOperatorName(): String {

        return context.userPreferencesDataStore.data
            .map { preferences ->

                preferences[
                    OPERATOR_NAME
                ] ?: ""
            }
            .first()
    }
}