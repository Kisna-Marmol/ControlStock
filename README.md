# 📦 ControlStock

Sistema de gestión de inventario móvil desarrollado en Android con backend en PHP y base de datos MySQL.

---

# 📖 Descripción

**ControlStock** es una aplicación móvil diseñada para pequeñas y medianas empresas que permite gestionar productos, ventas y movimientos de inventario desde un dispositivo Android.

El sistema se conecta a un servidor web mediante una **API REST**, permitiendo sincronización en tiempo real y acceso seguro a la información.

---

# 🚀 Tecnologías utilizadas

| Tecnología | Descripción |
|---|---|
| 📱 Android (Java) | Aplicación móvil |
| 🌐 PHP | Backend y API REST |
| 🗄️ MySQL | Base de datos |
| 🖥️ Apache (XAMPP / Hosting) | Servidor |
| 🔗 JSON | Comunicación entre app y servidor |

---

# ✨ Funcionalidades

## 🔐 Seguridad y usuarios
- Login con roles (**Administrador / Empleado**)
- Gestión de usuarios con control de accesos
- Perfil de usuario (cambio de contraseña)
- Bitácora del sistema (registro de acciones)

## 📦 Inventario
- Gestión de productos
  - Foto
  - GPS
  - Código QR
- Movimientos de inventario
  - Entradas
  - Salidas
- Notificaciones de stock bajo

## 🧾 Ventas
- Registro de ventas con carrito
- Facturación automática
- Ventas del día

## 📚 Catálogos
- Categorías
- Proveedores
- Unidades de medida

## 📊 Dashboard
- Estadísticas en tiempo real

---

# 🎨 Paleta de colores

| Uso            | Color     |
| -------------- | --------- |
| Azul principal | `#1E3A8A` |
| Azul claro     | `#3B82F6` |
| Verde acento   | `#10B981` |
| Fondo gris     | `#F3F4F6` |
| Texto oscuro   | `#1F2937` |

---

# 🗂️ Estructura del proyecto

```bash
ControlStock/
│
├── app/
│   ├── java/com/example/controlstock/
│   │
│   ├── clases/
│   │   ├── ApiService
│   │   ├── Config
│   │   ├── Dialog
│   │   └── Utils
│   │
│   ├── modelo/
│   │   ├── Cliente
│   │   └── DetalleVenta
│   │
│   ├── fragmentos/
│   │   ├── CategoriasFragment
│   │   ├── ProveedoresFragment
│   │   └── UnidadesFragment
│   │
│   └── *.Activity.java
│
├── res/
│   ├── layout/      # XMLs de pantallas e items
│   ├── drawable/    # Íconos y fondos
│   └── values/      # Colores, strings y temas
│
└── backend/
    ├── conexion.php
    ├── login.php
    ├── producto_*.php
    ├── ventas/
    ├── clientes/
    └── ...
```

---

# 👨‍💻 Autor

Kisna Marmol

---

# 📌 Notas
El sistema está diseñado para ser escalable y adaptable a distintos tipos de negocio.
Se recomienda usar HTTPS en producción.
Puede integrarse fácilmente con otros sistemas mediante API REST.
