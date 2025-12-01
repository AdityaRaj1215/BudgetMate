package com.personalfin.server.receipt.web;

import com.personalfin.server.expense.dto.ExpenseCreateRequest;
import com.personalfin.server.expense.service.ExpenseService;
import com.personalfin.server.expense.dto.ExpenseResponse;
import com.personalfin.server.receipt.dto.ReceiptScanRequest;
import com.personalfin.server.receipt.dto.ReceiptScanResponse;
import com.personalfin.server.receipt.service.ReceiptParserService;
import com.personalfin.server.receipt.service.ReceiptScannerService;
import com.personalfin.server.receipt.service.ReceiptParserService.ReceiptParsedData;
import jakarta.validation.Valid;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptScannerService scannerService;
    private final ReceiptParserService parserService;
    private final ExpenseService expenseService;

    public ReceiptController(
            ReceiptScannerService scannerService,
            ReceiptParserService parserService,
            ExpenseService expenseService) {
        this.scannerService = scannerService;
        this.parserService = parserService;
        this.expenseService = expenseService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ReceiptScanResponse> scanReceipt(
            @Valid @RequestBody ReceiptScanRequest request) {
        try {
            String ocrText = scannerService.extractTextFromBase64(request.imageData());
            ReceiptParsedData parsedData = parserService.parse(ocrText);

            ExpenseCreateRequest expenseRequest = new ExpenseCreateRequest(
                    "Receipt from " + (parsedData.getMerchant() != null ? parsedData.getMerchant() : "Unknown"),
                    parsedData.getMerchant(),
                    parsedData.getAmount(),
                    parsedData.getDate(),
                    parsedData.getCategory(),
                    null
            );

            ReceiptScanResponse response = new ReceiptScanResponse(
                    parsedData.getAmount(),
                    parsedData.getMerchant(),
                    parsedData.getDate(),
                    parsedData.getCategory(),
                    ocrText,
                    expenseRequest
            );

            return ResponseEntity.ok(response);
        } catch (TesseractException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/scan-and-create")
    public ResponseEntity<ExpenseResponse> scanAndCreateExpense(
            @Valid @RequestBody ReceiptScanRequest request) {
        try {
            String ocrText = scannerService.extractTextFromBase64(request.imageData());
            ReceiptParsedData parsedData = parserService.parse(ocrText);

            ExpenseCreateRequest expenseRequest = new ExpenseCreateRequest(
                    "Receipt from " + (parsedData.getMerchant() != null ? parsedData.getMerchant() : "Unknown"),
                    parsedData.getMerchant(),
                    parsedData.getAmount(),
                    parsedData.getDate(),
                    parsedData.getCategory(),
                    null
            );

            ExpenseResponse expense = expenseService.createExpense(expenseRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(expense);
        } catch (TesseractException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}









