package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.MediaUploadRequest;
import com.example.urbannest.security.FirebaseAuthFilter;
import com.example.urbannest.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(S3Controller.class)
@AutoConfigureMockMvc(addFilters = false)
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private FirebaseAuthFilter firebaseAuthFilter;

    @Test
    void getUploadUrl_validRequest_returns200() throws Exception {
        when(s3Service.generateUploadUrl(anyString(), anyString()))
                .thenReturn("https://s3.presigned/upload");

        MediaUploadRequest request = new MediaUploadRequest();
        request.setFileName("photo.jpg");
        request.setContentType("image/jpeg");
        request.setCategory("listings");

        mockMvc.perform(post("/s3/upload-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://s3.presigned/upload"))
                .andExpect(jsonPath("$.key").exists());
    }

    @Test
    void getUploadUrl_missingFields_returns400() throws Exception {
        MediaUploadRequest request = new MediaUploadRequest();

        mockMvc.perform(post("/s3/upload-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDownloadUrl_returns200() throws Exception {
        when(s3Service.generateDownloadUrl("listings/photo.jpg"))
                .thenReturn("https://s3.presigned/download");

        mockMvc.perform(get("/s3/download-url")
                        .param("key", "listings/photo.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://s3.presigned/download"));
    }

    @Test
    void deleteObject_returns204() throws Exception {
        mockMvc.perform(delete("/s3")
                        .param("key", "listings/photo.jpg"))
                .andExpect(status().isNoContent());

        verify(s3Service).deleteObject("listings/photo.jpg");
    }
}
