package co.edu.unbosque.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = Logger.getLogger(FirebaseConfig.class.getName());

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project.id:}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            logger.warning("Firebase: FIREBASE_CREDENTIALS_PATH no configurado — FCM desactivado.");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            InputStream serviceAccount = resolverCredenciales();
            if (serviceAccount == null) {
                logger.warning("Firebase: Archivo de credenciales no encontrado en: " + credentialsPath);
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();
            FirebaseApp.initializeApp(options);
            logger.info("Firebase inicializado correctamente (proyecto: " + projectId + ").");
        } catch (Exception e) {
            logger.warning("Firebase: Error al inicializar — " + e.getMessage()
                    + ". FCM desactivado; el resto del sistema continúa normalmente.");
        }
    }

    private InputStream resolverCredenciales() throws Exception {
        // Primero intenta como ruta absoluta/relativa en el sistema de archivos
        File file = new File(credentialsPath);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        // Si no existe como fichero, intenta como recurso en el classpath
        InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(credentialsPath);
        if (classpathStream != null) {
            logger.warning("Firebase: Credenciales cargadas desde classpath (" + credentialsPath
                    + "). Para producción usa la variable de entorno FIREBASE_CREDENTIALS_PATH con una ruta externa.");
        }
        return classpathStream;
    }
}
