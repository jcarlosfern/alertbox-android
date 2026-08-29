# AlertBox Android

Aplicación Android nativa de AlertBox, construida con Kotlin, Jetpack Compose,
Firebase Authentication y Firebase Cloud Messaging. Su paquete de producción es
`app.alertbox.io`; la variante de desarrollo usa `app.alertbox.io.debug`.

## Funcionalidad

- Alta, acceso, recuperación y verificación de correo con Firebase.
- Acceso con Google y Apple mediante Firebase Authentication.
- Perfil y preferencias de intereses, push, correo, mensajes privados y marketing.
- Horas de descanso, avisos críticos y modo viaje.
- Inicio, bandeja, organizaciones, canales, promociones, sorteos y encuestas.
- Programas de fidelización, recompensas, guardados y enlaces profundos.
- Notificaciones FCM con navegación directa al aviso.
- Eliminación de cuenta protegida por confirmación y autenticación reciente.

## Requisitos

- Android Studio con JDK 17.
- Android SDK 36.
- Node.js/npm (solo para los comandos homogéneos de validación).
- Acceso al proyecto Firebase `alertbox-42dad`.

## Configuración Firebase

La app compila sin credenciales y muestra un estado de configuración seguro. Para
activar autenticación y notificaciones al 100 %, registra dos aplicaciones Android
en Firebase:

1. Producción: paquete `app.alertbox.io`.
2. Desarrollo: paquete `app.alertbox.io.debug`.

Descarga cada `google-services.json` desde la consola y colócalo respectivamente en:

- `app/src/release/google-services.json`
- `app/src/debug/google-services.json`

Estos archivos están ignorados por Git. En Firebase Authentication deben estar
habilitados Correo/contraseña, Google y Apple. Para Google añade las huellas SHA-1
y SHA-256 de las claves de desarrollo y publicación. Para FCM configura también la
credencial APNs de iOS en el mismo proyecto, sin reutilizar claves privadas dentro
de esta aplicación.

## Desarrollo y validación

```bash
npm run lint
npm test
npm run build
```

El APK de desarrollo se genera en `app/build/outputs/apk/debug/`. La integración
continua repite las tres comprobaciones en cada cambio de `main` y pull request.

## Arquitectura

- `core/auth`: ciclo de sesión y proveedores Firebase.
- `core/network`: cliente HTTPS autenticado y contrato REST.
- `data`: repositorio y sincronización con FCM.
- `ui`: estado unidireccional, navegación y pantallas Compose.

Consulta [SECURITY.md](SECURITY.md) antes de configurar firma o automatizaciones
de publicación.
