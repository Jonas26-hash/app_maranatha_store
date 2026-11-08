<div align="center">

# Maranatha Store — App Móvil

**Kotlin Multiplatform + Compose Multiplatform** · Android & iOS · Conectada al backend real

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.11.1-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![Ktor](https://img.shields.io/badge/Ktor_Client-3.5.2-087CFA)](https://ktor.io/)
[![Koin](https://img.shields.io/badge/Koin-4.2.2-F7C948)](https://insert-koin.io/)
[![Android](https://img.shields.io/badge/Android-minSdk_24_|_target_36-3DDC84?logo=android&logoColor=white)](#)
[![iOS](https://img.shields.io/badge/iOS-KMP_|_SwiftUI-000000?logo=apple&logoColor=white)](#)
[![Tests](https://img.shields.io/badge/tests-109_passed-2ea44f)](#-pruebas)

<br/>

> Base móvil para la operación diaria de **Maranatha Store**: autenticación real, sesión segura y módulos conectados al API Gateway.  
> El backend es la **única fuente de verdad** para stock, precios, totales y anulaciones.

<br/>

[Inicio rápido](#-inicio-rápido) · [Arquitectura](#-arquitectura) · [Backend](#-backend) · [Estado](#-estado-del-proyecto) · [Contribuir](#-contribuir)

</div>

---

## Tabla de contenido

- [Visión general](#-visión-general)
- [Stack tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Inicio rápido](#-inicio-rápido)
- [Backend](#-backend)
- [Comandos útiles](#-comandos-útiles)
- [Estado del proyecto](#-estado-del-proyecto)
- [Contrato y límites del backend](#-contrato-y-límites-del-backend)
- [Seguridad](#-seguridad)
- [Pruebas](#-pruebas)
- [Contribuir](#-contribuir)

---

## Visión general

| | |
|---|---|
| **Plataformas** | Android + iOS (código compartido en `shared/`) |
| **UI** | Compose Multiplatform / Material 3 |
| **Estado** | `StateFlow` + MVVM |
| **DI** | Koin |
| **Red** | Ktor Client (OkHttp Android · Darwin iOS) |
| **Backend** | Maranatha-Store-V2 vía API Gateway |

### Alcance actual
- ✅ Login real (`POST /auth/login`) y sesión persistente cifrada.
- ✅ Restauración de sesión y expiración automática ante `401` protegido.
- ✅ Productos / inventario (listado, CRUD, activar/desactivar, ajustes de stock).
- 🚧 Clientes, nueva venta, historial y dashboard derivado.

### Fuera de alcance (v1)
Modo offline, ventas en cola, base comercial local, reportes avanzados y cambios en backend/web.

---

## Stack tecnológico

| Componente | Versión |
|---|---:|
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1 |
| Material 3 (Compose) | 1.11.0-alpha07 |
| Android Gradle Plugin | 9.3.3 |
| Gradle | 9.1.0 |
| Ktor Client | 3.5.2 |
| Kotlinx Coroutines | 1.10.2 |
| Koin | 4.2.2 |
| AndroidX Lifecycle | 2.11.0-beta01 |
| Android SDK | min 24 · compile/target 36 |
| Java toolchain | 21 (bytecode JVM 11) |

---

## Arquitectura

```text
androidApp/                  Android: Application, Activity, tema, manifest
iosApp/                      SwiftUI, Xcode project, configuración
shared/
  commonMain/
    data/remote/             Ktor, DTOs, APIs, HttpClientFactory, errores HTTP
    data/auth/               GestorSesion (StateFlow)
    data/security/           Serialización de sesión para almacén seguro
    domain/                  Modelos, interfaces y errores de negocio
    presentation/            Pantallas y ViewModels
    navigation/              Destinos del menú lateral
    theme/                   Tema Material 3 de marca
    di/                      Módulos Koin compartidos
  androidMain/               OkHttp, Keystore AES/GCM, DI Android
  iosMain/                   Darwin, Keychain, DI iOS
  commonTest/                Tests unitarios (MockEngine, dobles de prueba)
buildSrc/                    Tarea Gradle: genera ConfigApi (URL base)
```

<details>
<summary><strong>Flujo de autenticación</strong></summary>

1. `LoginViewModel` valida campos y evita dobles envíos.
2. `AuthRepositorioRemoto` llama `POST /auth/login` con `{username, password}`.
3. La respuesta `{token, username, rol}` se mapea a `Sesion`.
4. `GestorSesion` guarda la sesión cifrada (Keystore/Keychain) y expone `StateFlow`.
5. `HttpClientFactory` adjunta JWT `Bearer` en rutas protegidas (nunca al login).
6. Un `401` en ruta protegida limpia la sesión local → vuelve al login.
7. No existe `POST /auth/me`: la sesión se restaura localmente desde datos cifrados.

</details>

---

## Inicio rápido

### Requisitos

| | |
|---|---|
| **JDK** | 21 |
| **Android** | Android Studio + SDK 36 |
| **iOS** | macOS + Xcode |
| **Backend** | Maranatha-Store-V2 accesible |

### Clonar

```bash
git clone https://github.com/Jonas26-hash/app_maranatha_store.git
cd app_maranatha_store
```

### Compilar / instalar

```powershell
# APK debug
.\gradlew.bat :androidApp:assembleDebug

# Instalar en dispositivo/emulador conectado
.\gradlew.bat :androidApp:installDebug
```

APK generado: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

**iOS:** abrir `iosApp/iosApp.xcodeproj` en Xcode y ejecutar en simulador/dispositivo (requiere macOS).

---

## Backend

- La app consume la **raíz del API Gateway** (`http://host:8080`), no `/api` ni `/api/v1`.
- La URL base se resuelve en **tiempo de compilación** mediante la propiedad Gradle `maranathaApiBaseUrl` y se genera en `ConfigApi.BASE_URL` (ver `buildSrc/.../GenerarConfigApiTask.kt` y `shared/build.gradle.kts`).  
  Si tu entorno requiere otra URL, configúrala localmente al compilar con `-PmaranathaApiBaseUrl=...` (sin versionar valores locales).
- **Android (dev):** el tráfico HTTP en claro está permitido por `androidApp/src/main/res/xml/network_security_config.xml`. En producción se debe usar **HTTPS** y restringir esa configuración.

---

## Comandos útiles

```powershell
# Compilar Android debug
.\gradlew.bat :androidApp:assembleDebug

# Instalar en dispositivo conectado
.\gradlew.bat :androidApp:installDebug

# Tests unitarios compartidos (host Android)
.\gradlew.bat :shared:testAndroidHostTest
```

---

## Estado del proyecto

| Módulo | Estado | Detalle |
|---|:---:|---|
| Autenticación / sesión | ✅ | Login real, sesión cifrada, restore, logout, `401` protegido |
| Productos / inventario | ✅ | Listado, búsqueda/filtros, CRUD, activar/desactivar, reponer/descontar stock |
| Clientes | 🚧 | Listado, alta, edición, eliminación |
| Nueva venta | 🚧 | Carrito, validación contra stock real, registro |
| Historial | 🚧 | Ventas, detalle, anulación, comprobantes |
| Inicio / dashboard | 🚧 | Indicadores derivados de datos reales (sin endpoint de reportes) |
| Offline | ⛔ | No implementado en v1 |
| Pruebas UI/instrumentadas | 🚧 | Pendiente |

<details>
<summary><strong>Decisiones clave (resumen)</strong></summary>

- **Online-only (v1):** evita prometer ventas offline sin sincronización/idempotencia acordada.
- **Stock/precios/totales:** los gobierna el backend; la app no duplica reglas.
- **URL por Gradle property:** configurada en build (ver `buildSrc` / `shared/build.gradle.kts`), sin versionar config sensible.
- **Restricción UI por rol:** acciones de gestión de productos se muestran a `ADMIN`; la autorización fina vive en el backend.

</details>

---

## Contrato y límites del backend

> Basado en el backend real.

- Login: `POST /auth/login` → `{token, username, rol}`; roles `ADMIN`/`USER` (desconocido → `USER`).
- Sin `/auth/me`; sesión restaurada localmente.
- Errores: `{timestamp, status, message}`.
- Sin endpoints de categorías, paginación ni reportes.
- Productos: `{id, nombre, descripcion, precio, stock, activo, version}`. Backend permite precio `0`; la app exige `> 0` (diferencia documentada).
- Borrados físicos (hard delete) en productos/clientes.
- Stock bajo (indicador UI): `<= 5` (derivado).

Estas limitaciones **no** se resuelven con datos falsos ni base comercial independiente.

---

## Seguridad

- JWT/usuario en almacén seguro por plataforma (**Keystore AES/GCM** Android, **Keychain** iOS). Nunca en texto plano ni en el repo.
- No se registran contraseñas, tokens ni `Authorization` (logging saneado).
- `.gitignore` excluye `local.properties`, `build/`, `.gradle/`, keystores, secretos y artefactos locales.
- Sin credenciales/endpoints embebidos en código fuente.
- HTTP en claro solo para desarrollo local; **producción requiere HTTPS**.
- Logout limpia almacén cifrado y caché de tokens en memoria.

---

## Pruebas

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

**Resultado verificado:** `109 tests completed, 0 failed` — `BUILD SUCCESSFUL`.

- Tests usan `MockEngine`; no requieren backend ni BD real.
- Cobertura: contrato auth/sesión, errores HTTP, validaciones y flujos principales (login, productos), grafo Koin con dobles.

---

## Contribuir

1. Crea tu rama desde `main`:
   ```bash
   git checkout main
   git pull
   git checkout -b feature/mi-cambio
   ```
2. Mantén el backend como fuente de verdad; no dupliques reglas de negocio.
3. Respeta la arquitectura por capas (`domain` → `data` → `presentation`).
4. Ejecuta pruebas antes de abrir PR:
   ```powershell
   .\gradlew.bat :shared:testAndroidHostTest
   ```
5. No subas secretos (`local.properties`, keystores, tokens). Revisa `git status` antes de commitear.

---

<div align="center">
<sub>Hecho con Kotlin Multiplatform para el equipo de Maranatha Store.</sub>
</div>
