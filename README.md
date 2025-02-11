# Documentación Técnica del Sistema de Transferencia de Archivos

Este documento proporciona una descripción técnica detallada del sistema de transferencia de archivos, incluyendo sus clases, métodos y funcionalidades. El sistema permite a los clientes conectarse a un servidor para subir, descargar y listar archivos.

## 1. Clase `Constants`

**Paquete:** `net.salesianos.utils`

Esta clase define constantes utilizadas en toda la aplicación.

### Constantes

*   `SERVER_PORT`:  Puerto del servidor (8082).  `int`.  Define el puerto en el que el servidor escucha las conexiones entrantes.
*   `USER_FILES_DIRECTORY`:  Directorio de archivos del cliente ("src\\net\\salesianos\\client\\client\_files").  `String`.  Especifica la ruta donde el cliente almacena los archivos que puede subir y donde se guardarán los archivos descargados.
*   `SERVER_FILES_DIRECTORY`: Directorio de archivos del servidor ("src\\net\\salesianos\\server\\server\_files"). `String`. Especifica la ruta donde el servidor almacena los archivos que los clientes pueden descargar y donde se guardarán los archivos subidos.

## 2. Clase `FileTransferUtil`

**Paquete:** `net.salesianos.utils`

Esta clase proporciona métodos de utilidad para la transferencia de archivos (envío y recepción).  Es una clase utilitaria, por lo que todos sus métodos son estáticos.

### Métodos

*   `sendFile(DataOutputStream dataOutputStream, File file)`: Envía un archivo al flujo de salida proporcionado.
    *   **Parámetros:**
        *   `dataOutputStream`: `DataOutputStream`. Flujo de salida de datos al que se enviará el archivo.
        *   `file`: `File`.  El archivo que se va a enviar.
    *   **Lanza:** `IOException` Si ocurre un error de entrada/salida durante el envío.
    *   **Funcionamiento:**
        1.  Lee la longitud del archivo.
        2.  Crea un buffer de lectura.
        3.  Lee el archivo en bloques y los escribe en el `DataOutputStream`.
        4.  Asegura que todos los bytes se envíen con `flush()`.

*   `receiveFile(DataInputStream dataInputStream, String filename)`: Recibe un archivo desde el flujo de entrada proporcionado y lo guarda en la ruta especificada.
    *   **Parámetros:**
        *   `dataInputStream`: `DataInputStream`.  Flujo de entrada de datos del que se recibirá el archivo.
        *   `filename`: `String`.  Ruta completa (incluyendo nombre de archivo) donde se guardará el archivo recibido.
    *   **Lanza:** `IOException` Si ocurre un error de entrada/salida durante la recepción.
    *   **Funcionamiento:**
        1.  Crea la estructura de directorios si no existe, usando `mkdirs()`.
        2.  Lee la longitud del archivo desde el flujo de entrada.
        3.  Crea un buffer de escritura.
        4.  Lee el archivo en bloques desde el `DataInputStream` y lo escribe en el archivo de salida hasta que se hayan recibido todos los bytes.

## 3. Clase `ServerApp`

**Paquete:** `net.salesianos.server`

Esta clase representa la aplicación principal del servidor.  Gestiona la conexión de los clientes y crea hilos para manejar cada conexión.

### Método `main`

*   `main(String[] args)`:  Punto de entrada principal del servidor.
    *   **Parámetros:** `args`: `String[]`.  Argumentos de la línea de comandos (no utilizados).
    *   **Lanza:** `IOException` Si ocurre un error al iniciar el servidor o al aceptar conexiones.
    *   **Funcionamiento:**
        1.  Crea un `ServerSocket` en el puerto especificado por `Constants.SERVER_PORT`.
        2.  Entra en un bucle infinito:
            *   Espera a que un cliente se conecte (`serverSocket.accept()`).
            *   Crea un nuevo hilo `ClientHandler` para manejar la comunicación con el cliente.
            *   Inicia el hilo `ClientHandler`.

## 4. Clase `ClientHandler`

**Paquete:** `net.salesianos.server.threads`

Esta clase gestiona la comunicación con un cliente individual.  Se ejecuta en un hilo separado para cada cliente.

### Atributos

*   `clientSocket`: `Socket`.  El socket de conexión con el cliente.
*   `clientName`: `String`.  El nombre del cliente.

### Constructor

*   `ClientHandler(Socket socket)`:  Inicializa el `ClientHandler` con el socket del cliente.
    *   **Parámetros:** `socket`: `Socket`. El socket de conexión con el cliente.

### Método `run`

*   `run()`:  Método principal del hilo.  Gestiona la comunicación con el cliente.
    *   **Lanza:** `IOException` Si ocurre un error durante la comunicación con el cliente.
    *   **Funcionamiento:**
        1.  Obtiene los flujos de entrada y salida del socket.
        2.  Lee el nombre del cliente.
        3.  Entra en un bucle infinito para procesar los comandos del cliente:
            *   Lee el comando enviado por el cliente.
            *   **`QUIT`**:  Cierra la conexión con el cliente y termina el hilo.
            *   **`LIST`**:  Lista los archivos en el directorio del servidor y los envía al cliente.
            *   **`UPLOAD`**:  Recibe un archivo del cliente.
                *   Lee el nombre del archivo.
                *   Lee la confirmación de que el cliente está listo para subir el archivo.
                *   Recibe el archivo y lo guarda en el directorio del servidor, añadiendo el nombre del cliente al nombre del archivo para evitar colisiones.
            *   **`DOWNLOAD`**:  Envía un archivo al cliente.
                *   Lee el nombre del archivo solicitado.
                *  Comprueba si existe el archivo en el servidor
                *   Si el archivo existe, envía una confirmación al cliente y envía el archivo.
                *   Si el archivo no existe, envía un mensaje de error al cliente.
            *   Si se recibe un comando inválido, imprime un mensaje de error.
        4.  Captura excepciones de tipo `SocketException`, `EOFException` e `IOException`, mostrando mensajes de error apropiados.
        5.  Cierra el socket del cliente al finalizar.

## 5. Clase `ClientApp`

**Paquete:** `net.salesianos.client`

Esta clase representa la aplicación del cliente.  Permite al usuario conectarse al servidor y realizar operaciones de transferencia de archivos.

### Método `main`

*   `main(String[] args)`:  Punto de entrada principal del cliente.
    *   **Parámetros:** `args`: `String[]`.  Argumentos de la línea de comandos (no utilizados).
    *   **Lanza:** `IOException` Si ocurre un error al conectar con el servidor o durante la comunicación.
    *   **Funcionamiento:**
        1.  Crea un `Socket` para conectarse al servidor en `localhost` y el puerto especificado por `Constants.SERVER_PORT`.
        2.  Obtiene los flujos de entrada y salida del socket.
        3.  Crea un `Scanner` para leer la entrada del usuario desde la consola.
        4.  Solicita al usuario su nombre y lo envía al servidor.
        5.  Entra en un bucle infinito para interactuar con el servidor:
            *   Solicita al usuario que ingrese un comando (`UPLOAD`, `DOWNLOAD`, `LIST`, o `QUIT`).
            *   **`QUIT`**: Envía el comando al servidor y cierra la conexión.
            *   **`UPLOAD`**:
                *  Lista los archivos en la carpeta del cliente para facilitar la selección del archivo
                *   Solicita al usuario el nombre del archivo a subir.
                *   Envía el comando y el nombre del archivo al servidor.
                *   Si el archivo existe localmente, envía una confirmación al servidor y envía el archivo.
                *   Si el archivo no existe, envía un mensaje de error al servidor.
            *   **`DOWNLOAD`**:
                *   Solicita al usuario el nombre del archivo a descargar.
                *   Envía el comando y el nombre del archivo al servidor.
                *   Recibe la confirmación del servidor sobre la existencia del archivo.
                *   Si el archivo existe, lo recibe y lo guarda en el directorio del cliente.
                *   Si el archivo no existe, muestra un mensaje al usuario.
            *    **`LIST`**:
                * Envia el comando al servidor
                * Recibe la lista de archivos en el servidor y los muestra
            *   Si el comando no es válido, muestra un mensaje de error al usuario.
        6.  Cierra la conexión con el servidor.
        7.  Captura excepciones de tipo `IOException` y muestra mensajes de error.

## Flujo de la Aplicación

1.  **Inicio del Servidor:** El servidor se inicia y espera conexiones entrantes en el puerto 8082.
2.  **Inicio del Cliente:** El cliente se inicia y se conecta al servidor.
3.  **Identificación del Cliente:** El cliente envía su nombre al servidor.
4.  **Interacción:** El cliente envía comandos al servidor (UPLOAD, DOWNLOAD, LIST, QUIT). El servidor procesa los comandos y responde al cliente.
5.  **Transferencia de Archivos:**
    *   **UPLOAD:** El cliente envía un archivo al servidor.  El servidor lo guarda en su directorio de archivos, añadiendo el nombre del cliente al nombre original del archivo.
    *   **DOWNLOAD:** El cliente solicita un archivo del servidor. El servidor lo envía al cliente, quien lo guarda en su directorio de archivos.
    *   **LIST:** El cliente solicita la lista de los archivos del servidor. El servidor envía la lista de archivos.
6.  **Finalización:** El cliente envía el comando QUIT para cerrar la conexión. Tanto el cliente como el hilo del servidor que atendía al cliente terminan su ejecución.