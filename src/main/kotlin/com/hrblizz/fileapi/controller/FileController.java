package com.hrblizz.fileapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrblizz.fileapi.controller.exception.BadRequestException;
import com.hrblizz.fileapi.controller.exception.NotFoundException;
import com.hrblizz.fileapi.data.entities.Entity;
import com.hrblizz.fileapi.library.log.LogItem;
import com.hrblizz.fileapi.library.log.Logger;
import com.hrblizz.fileapi.rest.ErrorMessage;
import com.hrblizz.fileapi.rest.ResponseEntity;
import com.hrblizz.fileapi.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    FileStorageService storageService;
    Logger logger;

    @PostMapping("/files")
    public ResponseEntity<String> uploadFile(
            @RequestParam String name,
            @RequestParam String contentType,
            @RequestParam String meta,
            @RequestParam String source,
            @RequestParam(required = false) String expireTime,
            @RequestParam("content") MultipartFile file) throws JsonProcessingException {

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>(){};
        HashMap<String, Object> metaMap = new ObjectMapper().readValue(meta, typeRef);

        try {
            String token = storageService.save(name, contentType, metaMap, source, expireTime, file);
            return new ResponseEntity<>(token, 201);
        }
        catch (BadRequestException e) {
            return new ResponseEntity<>(null,
                    Collections.singletonList(new ErrorMessage(e.getMessage(),
                    Integer.toString(e.hashCode()))), 400);
        }
        catch (Exception e) {
            logger.crit(new LogItem(e.getMessage()));
            return new ResponseEntity<>(null,
                    Collections.singletonList(new ErrorMessage(e.getMessage(),
                            Integer.toString(e.hashCode()))), 503);
        }
    }

    @GetMapping("/files/metas")
    public ResponseEntity<List<Map<String, Map<String, Object>>>> getMetadatas(@RequestBody List<String> tokens){
        try{
            List<Map<String, Map<String, Object>>> metadata = storageService.loadMetadata(tokens);
            return new ResponseEntity<>(metadata, 200);
        }
        catch (BadRequestException e){
            return new ResponseEntity<>(null, Collections.singletonList(new ErrorMessage(e.getMessage(), Integer.toString(e.hashCode()))), 400);
        }
        catch (Exception e){
            logger.crit(new LogItem(e.getMessage()));
            return new ResponseEntity<>(null, Collections.singletonList(new ErrorMessage(e.getMessage(),
                    Integer.toString(e.hashCode()))), 503);
        }
    }

    @GetMapping("/file/{token}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Resource> downloadFile(@PathVariable String token,
                                                                          HttpServletResponse response){
        try {
            List<Object> fileData = storageService.load(token);
            Entity metaData = (Entity) fileData.get(1);
            Resource file = (Resource) fileData.get(0);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                    file.getFilename() + "\"");
            response.addHeader("X-Filename", metaData.fileName);
            response.addHeader("X-Filesize", Long.toString(metaData.getSize()));
            response.addHeader("X-CreateTime:", metaData.createTime);
            return org.springframework.http.ResponseEntity.ok().contentType(MediaType.parseMediaType(metaData.contentType)).body(file);
        }
        catch (BadRequestException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(null);
        }
        catch (Exception e){
            logger.crit(new LogItem(e.getMessage()));
            return org.springframework.http.ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/file/{token}")
    public ResponseEntity<Void> deleteFile(@PathVariable String token){
        try {
            storageService.delete(token);
            return new ResponseEntity<>(null, 200);
        }
        catch (NotFoundException e) {
            return new ResponseEntity<>(null, Collections.singletonList(new ErrorMessage(e.getMessage(),
                    Integer.toString(e.hashCode()))), 400);
        }
        catch (Exception e) {
            logger.crit(new LogItem(e.getMessage()));
            return new ResponseEntity<>(null, Collections.singletonList(new ErrorMessage(e.getMessage(),
                    Integer.toString(e.hashCode()))), 503);
        }
    }
}
