<div align="center">

# Compañeros

**Aplicación web para la gestión integral de pisos compartidos**

*Proyecto de Fin de Ciclo · Desarrollo de Aplicaciones Multiplataforma*

---

![Java](https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

</div>

---

## Descripción

**Compañeros** es una aplicación web desarrollada como Proyecto de Fin de Ciclo que centraliza la convivencia en pisos compartidos. Permite a los inquilinos gestionar tareas domésticas, repartir gastos, coordinar la lista de la compra y recibir notificaciones, todo desde una única plataforma accesible desde el navegador.

El proyecto aplica la arquitectura **MVC** con **Spring Boot** siguiendo los principios de separación de responsabilidades: controladores, capa de servicio, repositorios y entidades JPA bien diferenciados.

---

## Características principales

| Módulo | Descripción |
|---|---|
| **Gestión de la casa** | Crea una casa con código de invitación único o únete a una existente. El administrador puede habilitar/deshabilitar módulos y gestionar miembros. |
| **Tareas domésticas** | Asigna tareas a miembros con prioridad (alta, media, baja), fecha límite y seguimiento de estado (pendiente / completada / archivada). |
| **Gastos compartidos** | Registra gastos y repártelos automáticamente. Admite adjuntar justificantes con compresión automática de imagen. Estados: pendiente, confirmado y liquidado. |
| **Lista de la compra** | Lista colaborativa en tiempo real donde cualquier miembro puede añadir artículos y marcarlos como comprados. |
| **Notificaciones** | Sistema de notificaciones en la app (tareas asignadas, gastos añadidos, etc.) con actualización dinámica vía AJAX. |
| **Autenticación y roles** | Login seguro con contraseñas cifradas (BCrypt). Roles diferenciados: `ADMIN` y `MEMBER` con autorización basada en roles. |

---

## Arquitectura y tecnologías

```
┌─────────────────────────────────────────────────┐
│                   Navegador                      │
│         Thymeleaf · CSS · JavaScript             │
└─────────────────────┬───────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────┐
│              Spring Boot 2.7.5                   │
│  Spring MVC · Spring Security · Spring Data JPA  │
└─────────────────────┬───────────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────────┐
│               PostgreSQL 13+                     │
└─────────────────────────────────────────────────┘
```

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Java + Spring Boot | 11 / 2.7.5 |
| Seguridad | Spring Security + BCrypt | 5.7.4 |
| Persistencia | Spring Data JPA + Hibernate | 2.7.5 / 5.6.12.Final |
| Base de datos | PostgreSQL | 13+ |
| Frontend | Thymeleaf + CSS3 + JS | 3.0.15.RELEASE |
| Imágenes | Thumbnailator | 0.4.20 |
| Build | Apache Maven | 3.6+ |

---

## Estructura del proyecto

```
companeros-app/
├── src/
│   └── main/
│       ├── java/es/companeros/
│       │   ├── config/          # Configuración de Spring Security
│       │   ├── controller/      # Controladores MVC (casa, tareas, gastos…)
│       │   ├── model/           # Entidades JPA + enums (Role, Priority, ExpenseStatus…)
│       │   ├── repository/      # Repositorios Spring Data JPA
│       │   └── service/         # Lógica de negocio
│       └── resources/
│           ├── templates/       # Vistas Thymeleaf (.html)
│           └── static/          # CSS, JavaScript e imágenes
├── uploads/expenses/            # Justificantes subidos (ignorado en git)
└── pom.xml                      # Dependencias Maven
```

---

## Requisitos previos

- **Java** 11 o superior
- **Maven** 3.6 o superior
- **PostgreSQL** 13 o superior

---

## Instalación y puesta en marcha

### 1. Clona el repositorio

```bash
git clone https://github.com/victoreduc/companeros-app.git
cd companeros-app
```

### 2. Crea la base de datos

```sql
CREATE DATABASE companeros;
```

### 3. Configura la conexión a la base de datos

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/companeros
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

app.upload.dir=uploads/expenses
```

### 4. Compila y arranca

```bash
mvn spring-boot:run
```

### 5. Accede a la aplicación

Abre el navegador en → `http://localhost:8080`

---

## Despliegue en producción

Se recomienda externalizar la configuración mediante variables de entorno:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/companeros
export SPRING_DATASOURCE_USERNAME=usuario
export SPRING_DATASOURCE_PASSWORD=contraseña
export APP_UPLOAD_DIR=/ruta/absoluta/uploads
```

Y generar el `.jar` ejecutable con:

```bash
mvn clean package -DskipTests
java -jar target/companeros-app-*.jar
```

---

<div align="center">

Desarrollado como Proyecto de Fin de Ciclo · 2026

**Autor:** Víctor Gutiérrez · [contacto@victorgutierrez.dev](mailto:contacto@victorgutierrez.dev)

</div>
