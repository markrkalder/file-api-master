package com.hrblizz.fileapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrblizz.fileapi.controller.exception.BadRequestException;
import com.hrblizz.fileapi.controller.exception.NotFoundException;
import com.hrblizz.fileapi.data.entities.Entity;
import com.hrblizz.fileapi.data.repository.EntityRepository;
import com.hrblizz.fileapi.library.log.LogItem;
import com.hrblizz.fileapi.library.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileStorageServiceImpl implements FileStorageService{

    @Autowired
    private EntityRepository entityRepository;
    private Logger logger;
    private final Path root = Paths.get("uploads");

    @Override
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            logger.crit(new LogItem("Could not initialize a folder for upload " + e.getMessage()));
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public String save(String name,
                       String contentType,
                       Map<String, Object> meta,
                       String source,
                       String expireTime,
                       MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(name));
        }
        catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new BadRequestException("A file of that name already exists.");
            }
            logger.crit(new LogItem(e.getMessage()));
            throw new RuntimeException(e.getMessage());
        }

        String token = "";
        while (true) {
            token = UUID.randomUUID().toString();
            Optional<Entity> entity = entityRepository.findById(token);
            if (!entity.isPresent()) break;
        }
        Entity dbEntity = new Entity();
        dbEntity.fileName = name;
        dbEntity.token = token;
        dbEntity.contentType = contentType;
        dbEntity.source = source;
        dbEntity.createTime = LocalDateTime.now().toString();
        dbEntity.meta = meta;
        dbEntity.setSize(file.getSize());
        dbEntity.setExpireTime(expireTime);

        entityRepository.insert(dbEntity);

        return token;
    }

    @Override
    public List<Object> load(String token) {
        Optional<Entity> entity = entityRepository.findById(token);
        if (entity.isPresent()) {
            String fileName = entity.get().fileName;
            try {
                Path file = root.resolve(fileName);
                Resource resource = new UrlResource(file.toUri());

                if (resource.exists() || resource.isReadable()) {
                    return Arrays.asList(resource, entity.get());
                }
                else {
                    logger.crit(new LogItem("File with the token of " + token + " could not be read."));
                    throw new RuntimeException("Could not read the file!");
                }
            }
            catch (MalformedURLException e) {
                logger.crit(new LogItem("Error: " + e.getMessage()));
                throw new RuntimeException("Error: " + e.getMessage());
            }
        }
        else throw new NotFoundException("File with the given token was not found");
    }

    @Override
    public List<Map<String, Map<String, Object>>> loadMetadata(List<String> tokens) {
        List<Map<String, Map<String, Object>>> metaDataList = new ArrayList<>();
        for (String token : tokens) {
            Optional<Entity> entity = entityRepository.findById(token);
            if (entity.isPresent()){
                Map<String, Map<String, Object>> fileMeta = new HashMap<>();
                ObjectMapper objectMapper = new ObjectMapper();
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>(){};
                HashMap<String, Object> dataMap = objectMapper.convertValue(entity.get(), typeRef);
                dataMap.values().removeIf(Objects::isNull);
                fileMeta.put(token, dataMap);
                metaDataList.add(fileMeta);
            }
            else throw new NotFoundException("Could not find the file!");
        }
        return metaDataList;
    }

    @Override
    public void delete(String token) {
        Optional<Entity> entity = entityRepository.findById(token);
        if (entity.isPresent()) {
            String fileName = entity.get().fileName;
            try {
                Path file = root.resolve(fileName);
                Resource resource = new UrlResource(file.toUri());

                if (resource.exists()) {
                    FileSystemUtils.deleteRecursively(file.toFile());
                }
                else {
                    logger.crit(new LogItem("File couldn't be deleted, token: " + token));
                    throw new RuntimeException("Could not delete the file!");
                }
                entityRepository.delete(entity.get());
            }
            catch (MalformedURLException e) {
                throw new RuntimeException("Error: " + e.getMessage());
            }
        }
        else throw new NotFoundException("File with the given token was not found");
    }
}
