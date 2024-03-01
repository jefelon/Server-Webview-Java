# [Server-Webview-Java]
##  Índice
* [1. Introducción](#1-introducción)
* [2. Requisitos previos](#2-requisitos-previos)
* [3. Despliegue](#3-despliegue)
* [4. Subirlo al servidor web](#4-subirlo-al-servidor-web)
* [5. Configurar la URL de notificacion al final del pago](#5--configurar-la-url-de-notificaci%C3%B3n-al-final-del-pago)
## 1. Introducción
En este manual podrás encontrar una guía paso a paso para configurar un servidor de **[Java]** para generar un link de redirección. Te proporcionaremos instrucciones detalladas y credenciales de prueba para la instalación y configuración del proyecto, permitiéndote trabajar y experimentar de manera segura en tu propio entorno local.
Este manual está diseñado para ayudarte a comprender el flujo de la integración de la pasarela para ayudarte a aprovechar al máximo tu proyecto y facilitar tu experiencia de desarrollo.

<p align="center">
  <img src="https://i.postimg.cc/CKX3ZqWz/postman.png" alt="Popin" width="850"/>
</p>

<a name="Requisitos_Previos"></a>
 
## 2. Requisitos previos
* Comprender el flujo de comunicación de la pasarela. [Información Aquí](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/start.html)
* Extraer credenciales del Back Office Vendedor. [Guía Aquí](https://github.com/izipay-pe/obtener-credenciales-de-conexion)
  
> [!NOTE]
> Tener en cuenta que, para que el desarrollo de tu proyecto, eres libre de emplear tus herramientas preferidas.

## 3. Despliegue
### Instalar Apache Maven:
1. Descargar archivo binario del enlace:
```sh
https://maven.apache.org/download.cgi
```

2. Descomprimir el contenido
```sh
apache-maven-3.9.6-bin.zip
```

3. Añadir la ruta `..\apache-maven-3.9.6\bin` al PATH de las variables de entorno
<p align="center">
  <img src="https://i.postimg.cc/SsKrcSz3/entorno.png" alt="Entorno" width="400"/>
</p>

4. Confirma ejecutando en una terminal `mvn -v` :
  ```sh
  Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: /opt/apache-maven-3.9.6
Java version: 1.8.0_45, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.8.5", arch: "x86_64", family: "mac"
  ```
5. Documentación oficial. [Aquí](https://maven.apache.org/install.html)

### Clonar el proyecto:
  ```sh
  git clone [https://github.com/izipay-pe/Server-Webview-Java.git]
  ```
### Ejecutar proyecto
* Ingrese a la carpeta raíz del proyecto desde el terminal.

* Crear un archivo `jar` ejecutable:
```bash
mvn package
 ```
* Para poder probar si el servidor está operativo, ingrese a la carpeta `target` y ejecute el comando apuntando hacia el archivo `jar` con dependencias:
```bash
java -jar inApp-server-1.1-jar-with-dependencies.jar
```

## 4. Subirlo al servidor web

Para este ejemplo se utilizó reenvío de puertos en Visual Studio Code, siga los siguientes pasos.

  * Paso 1: Dirigirse a la pestaña de PORTS.
  * Paso 2: Iniciar sesión en GitHub en caso no ha iniciado sesión anteriormente.
  * Paso 3: Añadir el puerto de conexión que se desea sea accesible, en este caso el 9090.  
  * Paso 4: Cambiar la visibilidad a Públic, para permitir conexiones sin inicio de sesión. 

    <p align="center">
  <img src="https://i.postimg.cc/mD907rsV/visual.png" />
</p>

## 5. Probar el servidor desde POSTMAN

* Colocar la URL con el metodo POST y enviar la consulta.
  
 ```bash
https://qhm8pck8-9090.brs.devtunnels.ms/
```

* Datos a enviar en formato JSON raw:
 ```bash
{
    "email": "example12@gmail.com",
    "amount": "100",
    "currency": "604", //Soles
    "mode": "TEST",
    "language": "es",
    "orderId": "test-12"
}
```
