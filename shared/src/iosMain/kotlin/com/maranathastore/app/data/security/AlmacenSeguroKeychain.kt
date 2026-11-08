package com.maranathastore.app.data.security

import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.domain.security.AlmacenSeguro
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroKeychain(
    private val servicio: String = "com.maranathastore.app",
    private val cuenta: String = "sesion"
) : AlmacenSeguro {

    override fun guardarSesion(sesion: Sesion) {
        borrarSesion()

        val datos = (sesion.aJsonSeguro() as NSString)
            .dataUsingEncoding(NSUTF8StringEncoding)
            ?: return

        val consulta = nuevaConsulta()
        CFDictionaryAddValue(consulta, kSecValueData, CFBridgingRetain(datos))

        SecItemAdd(consulta, null)
        CFRelease(consulta)
    }

    override fun leerSesion(): Sesion? = memScoped {
        val consulta = nuevaConsulta()
        CFDictionaryAddValue(consulta, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(consulta, kSecMatchLimit, kSecMatchLimitOne)

        val resultado = alloc<CFTypeRefVar>()
        val estado = SecItemCopyMatching(consulta, resultado.ptr)
        CFRelease(consulta)

        if (estado != errSecSuccess) return@memScoped null

        val datos = CFBridgingRelease(resultado.value) as? NSData ?: return@memScoped null
        val texto = NSString.create(data = datos, encoding = NSUTF8StringEncoding) as String?
        texto?.aSesionSegura()
    }

    override fun borrarSesion() {
        val consulta = nuevaConsulta()
        SecItemDelete(consulta)
        CFRelease(consulta)
    }

    private fun nuevaConsulta(): CFMutableDictionaryRef {
        val consulta = CFDictionaryCreateMutable(kCFAllocatorDefault, 4, null, null)!!
        CFDictionaryAddValue(consulta, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(consulta, kSecAttrService, cfString(servicio))
        CFDictionaryAddValue(consulta, kSecAttrAccount, cfString(cuenta))
        return consulta
    }

    private fun cfString(valor: String) = memScoped {
        CFStringCreateWithCString(kCFAllocatorDefault, valor.cstr.ptr, kCFStringEncodingUTF8)
    }
}
