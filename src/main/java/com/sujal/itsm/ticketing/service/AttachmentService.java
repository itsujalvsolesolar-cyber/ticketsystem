package com.sujal.itsm.ticketing.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sujal.itsm.core.exception.FileStorageException;
import com.sujal.itsm.ticketing.model.Attachment;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.AttachmentRepository;

@Service
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;

  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  public AttachmentService(AttachmentRepository attachmentRepository) {
    this.attachmentRepository = attachmentRepository;
  }

  @Transactional
  public void uploadAttachments(Ticket ticket, MultipartFile[] files) {
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      try {
        Files.createDirectories(uploadPath);
      } catch (IOException e) {
        throw new FileStorageException("Could not create upload directory", e);
      }
    }

    for (MultipartFile file : files) {
      if (!file.isEmpty()) {
        try {
          String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
          Path targetLocation = uploadPath.resolve(uniqueFilename);
          Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

          Attachment attachment =
              Attachment.builder()
                  .filename(file.getOriginalFilename())
                  .storedFilename(uniqueFilename)
                  .contentType(file.getContentType())
                  .size(file.getSize())
                  .ticket(ticket)
                  .build();

          attachmentRepository.save(attachment);
        } catch (IOException e) {
          throw new FileStorageException("Could not store file " + file.getOriginalFilename(), e);
        }
      }
    }
  }

  public Resource downloadAttachmentResource(Long attachmentId) {
    Attachment attachment = getAttachmentMetadata(attachmentId);
    try {
      Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredFilename()).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (!resource.exists() || !resource.isReadable()) {
        throw new RuntimeException("Could not read the file!");
      }
      return resource;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  public Attachment getAttachmentMetadata(Long attachmentId) {
    return attachmentRepository
        .findById(attachmentId)
        .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
  }
}
