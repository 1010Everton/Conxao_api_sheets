package com.example.demo.Controller;

import com.example.demo.ApiS.SheetsQuickstart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class Tabble_interaction {
    private SheetsQuickstart sheetsQuickstart;
    @GetMapping("/updateSheet")
    public String retorna(){
        try {
            sheetsQuickstart.SheetData();
            return "retornado com sucesso";
        }
        catch(GeneralSecurityException|IOException e){
            e.printStackTrace();
            return "erro no retorno"+ e.getMessage();

        }
    }
}
