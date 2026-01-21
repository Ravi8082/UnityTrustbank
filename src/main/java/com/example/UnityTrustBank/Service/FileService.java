package com.example.UnityTrustBank.Service;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	
	String uploadfile(MultipartFile file, String path);
	InputStream getResourse(String path, String file);
	

}
