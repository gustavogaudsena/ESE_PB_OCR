package br.com.ocr.ocr_api.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageServiceInterface {
    public Object upload(MultipartFile file, String key);

    public Object read();

    public boolean delete();
}
