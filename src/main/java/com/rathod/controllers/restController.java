package com.rathod.controllers;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.rathod.entity.Expense;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
public class restController  {
  
	
	@Autowired
    private ExpenseService expenseService;
	
	
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
    	
        try {
        	
            PdfReader pdfReader = new PdfReader(file.getInputStream());
            StringBuilder text = new StringBuilder();

            for (int page = 1; page <= pdfReader.getNumberOfPages(); page++) {
                text.append(PdfTextExtractor.getTextFromPage(pdfReader, page));
            }
            pdfReader.close();

            String pdfText = text.toString();
            System.out.println(" pdftext is : "+pdfText);

            // Find the id, description, category, amount
            /*
            private Long id;
            private String description;
            private String category;
            private Double amount;
            */
            // Long id = exatractId(pdfText);  system will generated this 
            
            String description = extractElement(pdfText,"description:");
            String category = extractElement(pdfText,"category:");
            Double amount = Double.parseDouble(extractElement(pdfText,"amount:"));
            
            Expense expense = new Expense(description,category,amount);
            expenseService.createExpense(expense);
            return ResponseEntity.ok("Expense uploaded successfully");

            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading the PDF.");
        }
    
    }
    
    @GetMapping("/allExpenses")
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/total/{category}")
    public ResponseEntity<Double> getTotalExpensesByCategory(@PathVariable String category) {
        Double totalExpense = expenseService.calculateTotalExpensesByCategory(category);
        return ResponseEntity.ok(totalExpense);
    }
    
    
    
    public  String extractElement(String pdftext, String prefix) {
    	
        if (pdftext.contains(prefix)) {
            // Find the index where "Description:" ends
            int startIndex = pdftext.indexOf(prefix) + prefix.length();
            
            // Find the index of the comma (',') after the username
            int commaIndex = pdftext.indexOf(",", startIndex);

            // If a comma is found, extract the Description: up to the comma
            if (commaIndex != -1) {
                String username = pdftext.substring(startIndex, commaIndex).trim();
                return username;
            } else {
                // If no comma is found, extract the entire Description: part
                String username = pdftext.substring(startIndex).trim();
                return username;
            }
        } else {
            return null; // "Description:" not found in the input
        }	
   	
   }

 
      
}

