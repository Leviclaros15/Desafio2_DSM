# Desafío 2 - Agencia de Viajes "Explorar"

Aplicación móvil desarrollada en Kotlin para la gestión de destinos turísticos, integrando autenticación y servicios de base de datos con Firebase.

## Información del Alumno
* **Nombre:** Gerson Levi Claros Fuentes
* **Código:** CF250963

## Funcionalidades
1.  **Autenticación:**
    *   Registro de nuevos agentes de viajes.
    *   Inicio de sesión seguro mediante Firebase Auth.
2.  **Gestión de Destinos (CRUD):**
    *   **Crear:** Formulario para agregar nombre, país, precio, descripción e imagen.
    *   **Leer:** Catálogo en un RecyclerView con CardView y carga de imágenes mediante Glide.
    *   **Actualizar:** Edición de todos los campos de un destino existente.
    *   **Eliminar:** Borrado de destinos con confirmación previa.
3.  **Validaciones:**
    *   Campos obligatorios.
    *   Precio mayor a 0.
    *   Descripción de mínimo 20 caracteres.
    *   Imagen obligatoria para el registro.
    *   Manejo de errores mediante Toasts.

## Tecnologías Utilizadas
*   **Android Studio** & **Kotlin**
*   **Firebase Auth:** Autenticación de usuarios.
*   **Firebase Firestore:** Almacenamiento de datos NoSQL (incluye imágenes en formato Base64).
*   **Base64:** Codificación de imágenes para almacenamiento directo en Firestore.
*   **Material Design:** Interfaz de usuario basada en una paleta de colores personalizada.

## Enlaces
*   **Video de Defensa:** https://youtu.be/rj1zVuy3P20
*   **Repositorio GitHub:** https://github.com/Leviclaros15/Desafio2_DSM/edit/main/README.md

## Estructura de Commits
Se han seguido las convenciones de commits usando verbos en español/inglés:
*   `add`: Agregar nuevas funcionalidades o archivos.
*   `fix`: Corregir errores.
*   `update`: Actualizar configuraciones o dependencias.
*   `implement`: Implementar lógica de negocio.

---
*Nota: Asegúrate de agregar el archivo `google-services.json` en la carpeta `app/` para que la integración con Firebase funcione correctamente.*
