package com.maranathastore.app.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.auth.AuthRepository
import com.maranathastore.app.domain.auth.Credenciales
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, error = null) }
    }

    fun alternarContrasena() {
        _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    fun ingresar() {
        val estado = _uiState.value

        if (estado.cargando) return

        if (estado.usuario.isBlank() || estado.contrasena.isBlank()) {
            _uiState.update {
                it.copy(error = "Ingresa tu usuario y tu contraseña.")
            }
            return
        }

        // Se activa antes de lanzar la corrutina para que un segundo toque
        // inmediato no envíe dos solicitudes de login.
        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            try {
                val sesion = authRepository.iniciarSesion(
                    Credenciales(
                        usuario = estado.usuario.trim(),
                        contrasena = estado.contrasena
                    )
                )

                gestorSesion.establecer(sesion)

                _uiState.update {
                    it.copy(
                        cargando = false,
                        usuario = "",
                        contrasena = "",
                        mostrarContrasena = false
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: ErrorDeApi) {
                _uiState.update {
                    it.copy(cargando = false, error = fallo.mensaje)
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo iniciar sesión. Inténtalo de nuevo."
                    )
                }
            }
        }
    }
}
