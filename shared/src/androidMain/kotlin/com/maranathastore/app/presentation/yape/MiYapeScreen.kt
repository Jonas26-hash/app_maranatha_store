package com.maranathastore.app.presentation.yape
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.json.JSONObject

@Composable actual fun MiYapeScreen(adminToken: String) {
    val context = LocalContext.current
    val p = remember { prefs(context) }
    val scope = rememberCoroutineScope()
    var activo by remember { mutableStateOf(p.getString("token", null) != null) }
    var permiso by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("") }
    var ocupado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { while (true) {
        permiso = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.split(':')?.any { it.substringBefore('/') == context.packageName } == true
        estado = p.getString("estado", "Sin eventos") ?: "Sin eventos"
        delay(1000)
    } }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mi Yape", style = MaterialTheme.typography.headlineSmall)
        Text(if (activo && permiso) "Lector habilitado" else "Lector no habilitado")
        Text("Solo se reportan montos de notificaciones entrantes de Yape. No es confirmación bancaria.")
        Text(if (permiso) "Acceso a notificaciones concedido" else "Acceso a notificaciones pendiente")
        OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) { Text("Configurar permiso") }
        Button(enabled = permiso && !activo && !ocupado, onClick = { scope.launch {
            ocupado = true; error = ""
            try { val token = withContext(Dispatchers.IO) { JSONObject(request("dispositivo", "POST", adminToken)).getString("token") }; p.edit().clear().putString("token", token).commit(); activo = true }
            catch (e: Exception) { error = "No se pudo activar: ${e.message}" } finally { ocupado = false }
        } }) { Text("Activar receptor") }
        OutlinedButton(enabled = activo && !ocupado, onClick = { scope.launch {
            ocupado = true; error = ""
            try { withContext(Dispatchers.IO) { request("dispositivo", "DELETE", adminToken) }; p.edit().clear().commit(); activo = false }
            catch (e: Exception) { error = "No se pudo revocar en servidor: ${e.message}" } finally { ocupado = false }
        } }) { Text("Desactivar receptor") }
        Text(estado); if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
