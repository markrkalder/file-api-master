package com.hrblizz.fileapi.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileStorageService {
    public void init();

    public String save(String name,
                       String contentType,
                       Map<String, Object> meta,
                       String source,
                       String expireTime,
                       MultipartFile file);

    public List<Object> load(String token);

    public List<Map<String, Map<String, Object>>> loadMetadata(List<String> tokens);

    public void delete(String token);
}
