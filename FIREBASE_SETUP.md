# Configuración de Firebase

El proyecto usa **Firebase Authentication (anónimo)** y **Cloud Firestore**. Ya no consume DummyJSON.

## 1. Archivo de configuración

Descarga `google-services.json` desde Firebase Console para el package:

```text
com.sena.crud_dummyjson
```

y guárdalo en:

```text
app/google-services.json
```

El plugin `com.google.gms.google-services` valida que el package del JSON coincida con `applicationId`.

## 2. Firebase Console

### Authentication

1. **Authentication → Sign-in method**.
2. Habilita **Anonymous**.
3. Guarda.

### Firestore Database

1. **Build → Firestore Database → Create database**.
2. Publica reglas para la colección `productos`:

```js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /productos/{productId} {
      allow read: if request.auth != null;

      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.keys().hasAll([
          'nombre',
          'descripcion',
          'precio',
          'cantidad',
          'userId'
        ]);

      allow update, delete: if request.auth != null
        && resource.data.userId == request.auth.uid;
    }
  }
}
```

## 3. Estructura de datos

Colección: `productos`

Campos escritos por la app:

| Campo | Tipo | Descripción |
|---|---|---|
| `nombre` | String | Título mostrado en la UI |
| `descripcion` | String | Descripción |
| `precio` | Number | Precio |
| `cantidad` | Number | Cantidad (por defecto `0`) |
| `userId` | String | UID del usuario anónimo |
| `categoria` | String | Categoría mostrada en la UI |
| `imageUrl` | String | Imagen (vacía por defecto) |

El ID del documento de Firestore se usa como `ProductModel.id`.

## 4. Compilación

```powershell
.\gradlew.bat :app:assembleDebug
```

Los productos se escuchan en tiempo real con `addSnapshotListener`.
