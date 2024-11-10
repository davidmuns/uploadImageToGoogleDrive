package com.techwithden.backend;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

@org.springframework.stereotype.Service
public class Service {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACOUNT_KEY_PATH = getPathToGoodleCredentials();

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "cred.json");
        return filePath.toString();
    }

    public Res uploadImageToDrive(File file) throws GeneralSecurityException, IOException {
        Res res = new Res();

        try{
            String folderId = "1aoJTm7jtUnJLGkR3oPLegLcf4uEPyb6x";
            Drive drive = createDriveService();

            // Configurar metadata del archivo para Drive
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));

            // Detectar el tipo MIME del archivo
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // Tipo MIME genérico si no se detecta
            }
            // Crear FileContent con el tipo MIME detectado
            FileContent mediaContent = new FileContent(mimeType, file);

            // Subir archivo a Google Drive
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id").execute();

            // Crear URL de visualización del archivo
            String imageUrl = "https://drive.google.com/uc?export=view&id="+uploadedFile.getId();
            System.out.println("IMAGE URL: " + imageUrl);

            // Eliminar archivo local después de la carga
            file.delete();

            // Configurar respuesta
            res.setStatus(200);
            res.setMessage("Image Successfully Uploaded To Drive");
            res.setUrl(imageUrl);
        }catch (Exception e){
            System.out.println(e.getMessage());
            res.setStatus(500);
            res.setMessage(e.getMessage());
        }
        return  res;

    }

    private Drive createDriveService() throws GeneralSecurityException, IOException {

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .build();

    }
}
