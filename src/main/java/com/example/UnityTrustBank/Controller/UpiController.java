package com.example.UnityTrustBank.Controller;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.UnityTrustBank.Service.UpiService;
import com.example.UnityTrustBank.Repository.UpiRepo;
import com.example.UnityTrustBank.dto.UpiCreateDto;
import com.example.UnityTrustBank.dto.UpiPayDto;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@RestController
@RequestMapping("/upi")
public class UpiController {

    @Autowired
    private UpiService service;

    @Autowired
    private UpiRepo upiRepo;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody UpiCreateDto dto) {

        service.createUpi(
                dto.getAccountId(),
                dto.getVpa(),
                dto.getPin()
        );

        return ResponseEntity.ok("UPI created successfully");
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/pay")
    public ResponseEntity<String> pay(@RequestBody UpiPayDto dto) {

        service.pay(
                dto.getFromVpa(),
                dto.getToVpa(),
                dto.getPin(),
                dto.getAmount(),
                dto.getRemark()
        );

        return ResponseEntity.ok("UPI payment successful");
    }

    // ✅ BANK-GRADE QR (STATIC + DYNAMIC)
    @GetMapping("/qr")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam String vpa,
            @RequestParam(required = false) Double amount) throws Exception {

        // 🔒 Validate VPA
        if (!upiRepo.existsByVpa(vpa)) {
            throw new RuntimeException("VPA not found");
        }

        if (amount != null && amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        String upiUrl;

        if (amount == null) {
            upiUrl = "upi://pay?pa=" + vpa + "&cu=INR";
        } else {
            upiUrl = "upi://pay?pa=" + vpa + "&am=" + amount + "&cu=INR";
        }

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(upiUrl, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("inline", "upi-qr.png");

        return ResponseEntity.ok()
                .headers(headers)
                .body(stream.toByteArray());
    }
}
