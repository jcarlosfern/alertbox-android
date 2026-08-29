# Seguridad de AlertBox Android

## Principios aplicados

- Firebase Authentication gestiona credenciales y proveedores sociales; la app no almacena contraseñas.
- Todas las peticiones a la API exigen HTTPS y Android bloquea el tráfico en claro.
- El token Firebase se añade en memoria a cada petición y no se registra en logs.
- Las copias de seguridad y la extracción de datos de la aplicación están desactivadas.
- `google-services.json`, firmas y propiedades locales están excluidos del repositorio.
- El permiso de notificaciones se solicita en contexto y el marketing está desactivado por defecto.
- La eliminación de cuenta exige una confirmación explícita y autenticación reciente de Firebase.

## Reporte responsable

No abras una incidencia pública para una vulnerabilidad. Envía el informe a
`seguridad@alertbox.app` con pasos de reproducción, impacto y versión afectada.
No incluyas contraseñas, tokens, códigos de canje ni datos personales reales.

## Secretos

No se deben versionar:

- `google-services.json`
- almacenes de claves (`.jks`, `.keystore`)
- `local.properties`
- credenciales de cuentas de servicio

Los secretos de firma para publicación deben vivir en el almacén protegido del
entorno de CI o en Google Play App Signing.
