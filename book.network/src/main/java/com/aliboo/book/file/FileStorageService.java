package com.aliboo.book.file;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;

@Service
@Slf4j //hdi annotation bch nkhdmo b log (bsh ntl3o err ltht ra f if statement)
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}") //hia li dayrinha f yml
    private  String fileUploadPath;

    public String saveFile(@Nonnull MultipartFile sourceFile,@Nonnull Integer userId) {
        final String fileUploadSubPath = "users" + separator + userId ; //separator bch bla mnhtmo b details dl files wsh - wl _...

        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(@Nonnull MultipartFile sourceFile,@Nonnull String fileUploadSubPath) {

        final String finalUploadPath = fileUploadPath + separator + fileUploadSubPath ; // hada aykon 2akhir fileupload path li ayjm3 fih hd 3 li drna fo9
        //hna ant2kdo mn target folder
        File targetFolder = new File(finalUploadPath); //drna obj targetFolder mno3 File o drna fih finalUploadPath
        //hna andiro tcheck wsh deja endna hd l folder
        if(!targetFolder.exists()){
            //la mknsh kyn creatih lina (2asln ead ancreyiwh 3an tari9 targetFolder li drna fiha l path o mkdirs d spring li at cree lina folder )
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated){ //ola matcreash dir lia hd l err
                log.warn("Failed to create the target folder");
                return null; //bsh l kn l err yhbs mykmlsh hdshi li ltht
            }
        }
        // 3an tari9 sourceFile andiro file wsh ykon pnj pjj...
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());//getFileExtension method l tht

        String targetdFilePath = finalUploadPath + separator + System.currentTimeMillis() + "." + fileExtension;  //hna fin a savew target path bl ms bl ms bach mykonosh spase o chacter d . _è- ... o bsh ykon name unique bch mnin njiboh ykon unique mykonsh nfs name
        //bch flkhr file loacation and name will look ./upload/users/1/24556525855.jpg dok 2ar9am ra d ms

        Path targetPath = Paths.get(targetdFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved to {}", targetdFilePath);
            return targetdFilePath;
        } catch (IOException e) {
            log.error("File was not saved", e);
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()){ //lmknsh l file rej3 lia walo
            return "";
        }
        //smia.jpg hna ltht anjibo dik lkhra mn mor no9ta(.) .jpj .png ...

        int lastDoIndex = fileName.lastIndexOf("."); //bch tbda takhd mn mor . (soufiane.png ) atkhd png
        if (lastDoIndex == -1) { //y3ni ma endosh extention (ma endo la png la waloo)
            return "";//rj3 lia faragh
        }
        //substring ("unhappy".substring(2) returns "happy" "Harbison".substring(3) returns "bison") y3ni kat3tiha 9ima mnin tbda tkhd
        return fileName.substring(lastDoIndex + 1).toLowerCase(); // hna atakhd 9ima mn lastDoIndex o zid eliha whd (miphoto.png) lastDoIndex drnaha fo9 hia . o zid elia 1 y3ni png

    }
}
