# 💱 DivisasApp - Conversor de Monedas en Tiempo Real

![Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Design-Material%203-7B1FA2?style=for-the-badge&logo=materialdesign&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-orange?style=for-the-badge)
![Target SDK](https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-blue?style=for-the-badge)

**DivisasApp** es una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose** que permite realizar conversiones de divisas en tiempo real. Cuenta con integración de hardware del dispositivo para dictado por voz y vista previa de cámara.

---

## ✨ Características Principales

- 🪙 **Conversión en Tiempo Real**: Obtén las tasas de cambio más recientes utilizando la API pública de Exchange Rates (`open.er-api.com`).
- 🔀 **Intercambio Rápido (Swap)**: Cambia fácilmente la divisa de origen y destino con un solo toque.
- 🎙️ **Dictado por Voz**: Ingresa montos automáticamente usando el reconocimiento de voz nativo de Android (`SpeechRecognizer`).
- 📸 **Vista previa con Cámara**: Integración de cámara en vivo utilizando **CameraX**.
- 📊 **Lista de Tasas Rápidas**: Visualización simultánea de equivalencias para múltiples monedas populares (EUR, COP, MXN, GBP, USD, JPY, BRL, ARS, CAD, CHF).
- 🎨 **Interfaz Moderna**: Diseñada con **Material 3**, soporte para degradados dinámicos, tarjetas estilizadas e interfaz adaptativa.

---

## 🛠️ Tecnologías y Librerías Utilizadas

- **Lenguaje**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material 3**
- **Arquitectura**: MVVM (Model-View-ViewModel) con `State` y Corrutinas
- **Conexión a Red**: [Retrofit 2](https://square.github.io/retrofit/) + Gson Converter
- **Integración de Hardware**:
  - 🎥 **CameraX** (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`)
  - 🎤 **SpeechRecognizer** de Android para entrada de texto mediante voz
- **Inyección / Manejo de Estado**: `lifecycle-viewmodel-compose`

---

## 🌐 API de Divisas

La aplicación consume la API gratuita de tasas de cambio [Exchange Rates API (open.er-api.com)](https://open.er-api.com/):
```text
GET https://open.er-api.com/v6/latest/{base}
```

---

## 📱 Permisos Requeridos

La aplicación solicita dinámicamente los siguientes permisos según el uso de las funciones de hardware:

| Permiso | Propósito |
| :--- | :--- |
| `android.permission.INTERNET` | Consultar las tasas de cambio actualizadas de la API. |
| `android.permission.RECORD_AUDIO` | Dictar montos mediante comandos de voz. |
| `android.permission.CAMERA` | Mostrar la vista previa de la cámara en vivo dentro de un diálogo modal. |

---

## 🚀 Requisitos de Instalación

- **Android Studio**: Ladybug / 2024.2.1 o superior recomendado.
- **JDK**: Versión 17.
- **Dispositivo / Emulador**: Android 7.0 (API nivel 24) o posterior.

---

## 🔧 Configuración y Ejecución

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/TU_USUARIO/DivisasApp.git
   cd DivisasApp
   ```

2. **Abrir en Android Studio**:
   - Abre Android Studio y selecciona **Open**.
   - Navega hasta la carpeta del proyecto clonado y selecciónala.

3. **Sincronizar Gradle**:
   - Deja que Gradle descargue las dependencias automáticamente (o presiona `Sync Project with Gradle Files`).

4. **Ejecutar en un dispositivo o emulador**:
   - Conecta tu dispositivo Android con la Depuración USB activada o inicia un emulador.
   - Haz clic en el botón **Run (`Shift + F10`)**.

---

## 📂 Estructura del Proyecto

```text
DivisasApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/divisas/
│   │       │   └── MainActivity.kt       # UI en Compose, ViewModel y Lógica de Red/Hardware
│   │       └── AndroidManifest.xml        # Declaración de permisos y componentes
│   └── build.gradle.kts                   # Configuración del módulo app y dependencias
├── build.gradle.kts                       # Configuración raíz
├── settings.gradle.kts                    # Inclusión de módulos y repositorios
└── README.md                              # Documentación del proyecto
```

---



