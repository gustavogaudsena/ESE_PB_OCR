package br.com.ocr.ocr_api.service.storage;

public interface StorageService {
    Object upload(byte[] fileBytes, String key);

    Object read();

    boolean delete();
}
