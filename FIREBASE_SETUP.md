# Conectar Firebase (Cloud Firestore)

El proyecto ya tiene el código y las dependencias de Firebase. Para activarlo:

1. Entra a https://console.firebase.google.com/ y crea un proyecto.
2. Agrega una app Android con el package name:
   `com.sena.crud_dummyjson`
3. Descarga `google-services.json` y cópialo en:
   `app/google-services.json`
4. En la consola, habilita **Cloud Firestore** (Build > Firestore Database).
5. Ajusta las reglas de seguridad. Para pruebas locales puedes usar:

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /products/{productId} {
      allow read, write: if true;
    }
  }
}
```

> Para producción, cambia estas reglas por autenticación.

6. Sincroniza Gradle y ejecuta la app.

## Comportamiento

- Con `google-services.json`: el CRUD usa la colección `products` de Firestore.
- Sin ese archivo: el proyecto sigue compilando y el CRUD usa DummyJSON como fallback.

La inicialización de Firebase se hace de forma segura en `Application.kt`.
