package com.aliboo.book.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j //bsh nkhdmo b log. f IOException
public class FileUtils {
    public static byte[] readFileFromLocation(String fileUrl) {
        //hna andiro read l file
        if (StringUtils.isBlank(fileUrl)) { //3an tari9 StringUtils d spring shof lia l file li ay3ti l user f var fileUrl wsh khawi
            return null;
        }
        try{
            Path filePath = new File(fileUrl).toPath() ; //dir lia var mn no3 filePath o dir fiha l path dl user li aydiro fl var fileUrl
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("No file found in the path{}", fileUrl); // fileUrl l var li etana l user mfihash path
        }
        return null;
    }
}
