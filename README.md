# Sistema Admin - Clientes

## VM options

| Variable y valor                      | Descripción                                          |
|---------------------------------------|------------------------------------------------------|
| -Dio.ktor.development=true            | Define si la aplicación está en modo desarrollo.     |
| -Djava.library.path=[ruta opencv jar] | La ruta de la carpeta que contiene el jar de opencv. |

## Variables de entorno

| Variable                           | Descripción                                                            | Ejemplo                                      |
|------------------------------------|------------------------------------------------------------------------|----------------------------------------------|
| SID_DE_CUENTA_DE_TWILIO            | SID de la cuenta de Twilio.                                            | -                                            |
| AUTH_TOKEN_DE_CUENTA_DE_TWILIO     | Auth token de la cuenta de Twilio.                                     | -                                            |
| TELEFONO_DE_TWILIO_PARA_EL_SISTEMA | Número de un teléfono Twilio para enviar SMS.                          | -                                            |
| URL_DE_CONEXION_MONGO_DB           | Conexión a la base de datos MongoDB community (local).                 | mongodb://127.0.0.1:27017/?retryWrites=false |
| BASE_DE_DATOS                      | El nombre de la base de datos del sistema.                             | sistema_admin_clientes                       |
| EMAIL_DEL_SISTEMA                  | La dirección de una cuenta de Gmail para enviar emails.                | ejemplo@gmail.com                            |
| CONTRASENA_APP_GMAIL               | La contraseña de aplicación de una cuenta de Gmail para enviar emails. | -                                            |
| JWT_AUDIENCIA                      | A quién va dirigido los jwt.                                           | Sistema Admin - Clientes                     |
| JWT_EMISOR                         | Quién genera los jwt.                                                  | Sistema Admin - Clientes                     |
| JWT_REINO                          | El nombre del sistema.                                                 | Sistema Admin - Clientes                     |
| JWT_SECRETO                        | Cadena de caracteres para encriptar nuestro jwt.                       | miSuperSecreto123                            |

## Notas

- No se usan transacciones porque MongoDB community (local) está limitado
- Si bien se hashean las contraseñas, las imágenes de los rostros se guardan en texto plano (base64)