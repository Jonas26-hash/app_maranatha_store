package com.maranathastore.app.presentation.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maranathastore.app.data.remote.EvidenciaBetaDto
import com.maranathastore.app.data.remote.ResultadoLaboratorioDto
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.presentation.ventas.historial.QrEvidenciaBeta
import com.maranathastore.app.theme.LiquidGlass
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToLong
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

/** Confirma el carrito de la pantalla principal. No usa DNI, pago ni inventario comercial. */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun CatalogoBetaDialog(visible: Boolean,
    carrito: List<Pair<Producto,Int>>,
    cargar: suspend (String) -> EvidenciaBetaDto,
    emitir: suspend (String,String,Long) -> ResultadoLaboratorioDto,
    cerrar: () -> Unit,
    rechazoPrevio: () -> Unit,
    finalizar: () -> Unit) {
    var clave by rememberSaveable { mutableStateOf("") }
    var itemsReservados by rememberSaveable { mutableStateOf("") }
    var totalReservado by rememberSaveable { mutableStateOf(0L) }
    var numero by rememberSaveable { mutableStateOf("") }
    var confirmado by rememberSaveable { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var evidencia by remember { mutableStateOf<EvidenciaBetaDto?>(null) }
    val scope=rememberCoroutineScope()
    val items=carrito.sortedBy { it.first.id }.joinToString(",") { "${it.first.id}:${it.second}" }
    val total=carrito.sumOf { (p,q) -> (p.precio.comoDouble*100).roundToLong()*q }
    val valido=carrito.isNotEmpty() && carrito.size<=50 && carrito.all { it.second in 1..10 } && total>0
    fun soles(n:Long)="S/ ${n/100}.${(n%100).toString().padStart(2,'0')}"
    suspend fun leer(n:String) {
        val v=cargar(n)
        if(v.ambiente!="BETA" || v.validezFiscal || v.comprobante!="F9B1-$n" ||
            v.estadoSunat!="ACEPTADO_BETA" || v.codigo!="0" || v.contenidoQr.isBlank()) {
            error="Evidencia no confirmada; no se imprime como aceptada."; return
        }
        evidencia=v; confirmado=true
    }
    fun procesar(){
        if(ocupado || confirmado) return
        val nueva = clave.isBlank()
        if(nueva) {
            if(!valido) return
            clave=Uuid.random().toString(); itemsReservados=items; totalReservado=total
        }
        val op=clave; val datos=itemsReservados; val monto=totalReservado
        ocupado=true; error=null; evidencia=null
        scope.launch {
            try {
                val r=emitir(op,datos,monto)
                if(r.ambiente!="BETA" || r.validezFiscal || r.serie!="F9B1" || !r.numero.matches(Regex("[0-9]{8}"))) {
                    error="Respuesta inconsistente. Conserva la clave."
                } else {
                    numero=r.numero
                    if(r.estadoSunat=="ACEPTADO_BETA" && r.codigo=="0") leer(numero)
                    else error="F9B1-$numero: ${r.estadoSunat}. No se reenvia automaticamente."
                }
            } catch(e:CancellationException){throw e}
            catch(e:Exception){
                val status=(e as? ResponseException)?.response?.status?.value
                if(status==422 && nueva) {
                    // El controlador devuelve 422 antes de reservar la venta o enviar el XML.
                    clave="";itemsReservados="";totalReservado=0;numero="";confirmado=false
                    rechazoPrevio()
                    error="El carrito no coincide con la copia Beta o excede los limites. Actualiza la copia autorizada y revisa el carrito. No se emitio."
                } else error="Resultado no confirmado${status?.let { " (HTTP $it)" }.orEmpty()}. Conserva la clave."
            } finally { ocupado=false }
        }
    }
    fun cerrarSeguro(){
        if(ocupado) return
        if(confirmado && evidencia!=null){
            clave="";itemsReservados="";totalReservado=0;numero="";confirmado=false;evidencia=null;error=null
            finalizar()
        } else if(clave.isBlank()) cerrar()
        else cerrar() // conservar clave y carrito para recuperar la misma operacion
    }
    if(!visible) return
    LaunchedEffect(visible,numero,confirmado){
        if(confirmado && numero.isNotBlank() && evidencia==null && !ocupado){
            ocupado=true
            try{leer(numero)}catch(e:CancellationException){throw e}catch(_:Exception){error="No se pudo recuperar evidencia."}finally{ocupado=false}
        }
    }
    AlertDialog(onDismissRequest={ if(!ocupado) cerrarSeguro() },containerColor=LiquidGlass.SuperficieDialogo,
        title={Text("Confirmar venta de prueba",color=LiquidGlass.Texto)},
        text={Column(Modifier.heightIn(max=540.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("FACTURA BETA · Comprador ficticio. No realiza cobro, no descuenta stock comercial y no tiene validez fiscal.",color=LiquidGlass.TextoSuave)
            if(clave.isBlank() || items==itemsReservados) carrito.forEach{(p,q)->Text("$q x ${p.nombre} · ${p.precio.formateado()}",color=LiquidGlass.Texto)}
            else Text("Carrito original reservado; no se sustituira por la seleccion actual.",color=LiquidGlass.Peligro)
            Text("Total de prueba: ${soles(if(clave.isBlank()) total else totalReservado)}",color=LiquidGlass.VerdeNeon)
            if(!valido && clave.isBlank()) Text("Maximo 50 articulos distintos y 10 unidades por articulo en una factura Beta.",color=LiquidGlass.Peligro)
            if(clave.isNotBlank()) Text("Operacion: $clave",color=LiquidGlass.TextoSuave)
            if(numero.isNotBlank()) Text("F9B1-$numero",color=LiquidGlass.Texto)
            if(ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let{Text(it,color=LiquidGlass.Peligro)}
            if(!confirmado) Button(onClick={procesar()},enabled=!ocupado&&(valido||clave.isNotBlank()),modifier=Modifier.fillMaxWidth()){
                Text(if(clave.isBlank()) "Confirmar y emitir factura Beta" else "Recuperar misma operacion")
            }
            if(numero.isNotBlank()&&!ocupado&&evidencia==null) Button(onClick={
                ocupado=true;error=null;scope.launch{try{leer(numero)}catch(e:CancellationException){throw e}catch(_:Exception){error="No se pudo recuperar evidencia."}finally{ocupado=false}}
            }){Text("Consultar XML/CDR archivados")}
            evidencia?.let{v->
                Text("SUNAT: Aceptado en Beta · Codigo ${v.codigo}",color=LiquidGlass.VerdeNeon)
                Text("IGV S/ ${v.igv} · Total S/ ${v.total}",color=LiquidGlass.Texto)
                v.detalles.forEach{Text("${it.cantidad} x ${it.descripcion} · S/ ${it.importe}",color=LiquidGlass.Texto)}
                QrEvidenciaBeta(v.contenidoQr)
                val ref=VentaDto(serie=v.comprobante.substringBefore('-'),numero=v.comprobante.substringAfter('-'))
                val ticket=recordarImpresionComprobante(ref,"TICKET",beta=v)
                val a4=recordarImpresionComprobante(ref,"A4",beta=v)
                if(ticket!=null) Button(onClick=ticket,modifier=Modifier.fillMaxWidth()){Text("Guardar Ticket 80 mm / PDF")}
                if(a4!=null) Button(onClick=a4,modifier=Modifier.fillMaxWidth()){Text("Imprimir A4 / PDF")}
            }
        }},confirmButton={TextButton(onClick={cerrarSeguro()},enabled=!ocupado){Text(if(confirmado) "Finalizar" else "Cerrar")}})
}
