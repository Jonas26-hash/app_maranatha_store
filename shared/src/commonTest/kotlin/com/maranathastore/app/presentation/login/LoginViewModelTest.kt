package com.maranathastore.app.presentation.login

import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.support.AlmacenSeguroFalso
import com.maranathastore.app.support.AuthRepositoryFalso
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var almacen: AlmacenSeguroFalso
    private lateinit var gestor: GestorSesion

    @BeforeTest
    fun preparar() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        almacen = AlmacenSeguroFalso()
        gestor = GestorSesion(almacen)
    }

    @AfterTest
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sin usuario o contrasena no se llama al backend`() = runTest(dispatcher) {
        val repositorio = AuthRepositoryFalso()
        val viewModel = LoginViewModel(repositorio, gestor)

        viewModel.onUsuarioChange("   ")
        viewModel.ingresar()
        advanceUntilIdle()

        assertEquals(0, repositorio.llamadas)
        assertEquals(
            "Ingresa tu usuario y tu contraseña.",
            viewModel.uiState.value.error
        )
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `el login exitoso guarda la sesion y limpia el formulario`() = runTest(dispatcher) {
        val sesion = Sesion("jwt", "admin", Rol.ADMIN)
        val repositorio = AuthRepositoryFalso(sesion = sesion)
        val viewModel = LoginViewModel(repositorio, gestor)

        viewModel.onUsuarioChange("  admin  ")
        viewModel.onContrasenaChange("secreto")
        viewModel.ingresar()

        assertTrue(viewModel.uiState.value.cargando)

        advanceUntilIdle()

        assertEquals("admin", repositorio.ultimasCredenciales?.usuario)
        assertEquals("secreto", repositorio.ultimasCredenciales?.contrasena)
        assertEquals(sesion, gestor.sesion.value)
        assertEquals(sesion, almacen.sesionGuardada)
        assertEquals("", viewModel.uiState.value.usuario)
        assertEquals("", viewModel.uiState.value.contrasena)
        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `un fallo de credenciales muestra el mensaje y no guarda sesion`() = runTest(dispatcher) {
        val repositorio = AuthRepositoryFalso(
            fallo = ErrorDeApi(ErrorApi.CredencialesInvalidas("Usuario o contraseña incorrectos."))
        )
        val viewModel = LoginViewModel(repositorio, gestor)

        viewModel.onUsuarioChange("admin")
        viewModel.onContrasenaChange("mal")
        viewModel.ingresar()
        advanceUntilIdle()

        assertEquals(
            "Usuario o contraseña incorrectos.",
            viewModel.uiState.value.error
        )
        assertNull(gestor.sesion.value)
        assertEquals(0, almacen.guardados)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `un fallo inesperado muestra un mensaje generico`() = runTest(dispatcher) {
        val repositorio = AuthRepositoryFalso(
            fallo = IllegalStateException("detalle técnico")
        )
        val viewModel = LoginViewModel(repositorio, gestor)

        viewModel.onUsuarioChange("admin")
        viewModel.onContrasenaChange("secreto")
        viewModel.ingresar()
        advanceUntilIdle()

        assertEquals(
            "No se pudo iniciar sesión. Inténtalo de nuevo.",
            viewModel.uiState.value.error
        )
        assertNull(gestor.sesion.value)
    }

    @Test
    fun `un doble toque inmediato solo envia una solicitud`() = runTest(dispatcher) {
        val pendiente = CompletableDeferred<Sesion>()
        val repositorio = AuthRepositoryFalso(respuesta = pendiente)
        val viewModel = LoginViewModel(repositorio, gestor)

        viewModel.onUsuarioChange("admin")
        viewModel.onContrasenaChange("secreto")
        viewModel.ingresar()
        viewModel.ingresar()
        runCurrent()

        assertEquals(1, repositorio.llamadas)
        assertTrue(viewModel.uiState.value.cargando)

        pendiente.complete(Sesion("jwt", "admin", Rol.ADMIN))
        advanceUntilIdle()

        assertEquals(1, repositorio.llamadas)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun `escribir de nuevo limpia el error anterior`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(AuthRepositoryFalso(), gestor)

        viewModel.ingresar()
        advanceUntilIdle()
        assertEquals(
            "Ingresa tu usuario y tu contraseña.",
            viewModel.uiState.value.error
        )

        viewModel.onUsuarioChange("a")
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `mostrar contrasena alterna su visibilidad`() {
        val viewModel = LoginViewModel(AuthRepositoryFalso(), gestor)

        assertFalse(viewModel.uiState.value.mostrarContrasena)
        viewModel.alternarContrasena()
        assertTrue(viewModel.uiState.value.mostrarContrasena)
        viewModel.alternarContrasena()
        assertFalse(viewModel.uiState.value.mostrarContrasena)
    }

    @Test
    fun `puede enviar exige ambos campos y que no haya carga en curso`() {
        val viewModel = LoginViewModel(AuthRepositoryFalso(), gestor)

        assertFalse(viewModel.uiState.value.puedeEnviar)
        viewModel.onUsuarioChange("admin")
        assertFalse(viewModel.uiState.value.puedeEnviar)
        viewModel.onContrasenaChange("secreto")
        assertTrue(viewModel.uiState.value.puedeEnviar)
        viewModel.ingresar()
        assertFalse(viewModel.uiState.value.puedeEnviar)
    }
}
