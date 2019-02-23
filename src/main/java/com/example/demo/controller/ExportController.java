package com.example.demo.controller;

import com.example.demo.entity.Client;
import com.example.demo.entity.Facture;
import com.example.demo.entity.LigneFacture;
import com.example.demo.service.ClientService;
import com.example.demo.service.FactureService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Controlleur pour réaliser les exports.
 */
@Controller
@RequestMapping("/")
public class ExportController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private FactureService factureService;

    @GetMapping("/clients/csv")
    public void clientsCSV(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"clients.csv\""); // là je lui dis qu'il va faire un fichier à télécharger et qu'il s'appelle clients.csv
        PrintWriter writer = response.getWriter(); // je récupère le writter de la réponse Http
        writer.println("Matricule;Prénom; Nom; Date de naissance; Âge");
        List<Client> clients = clientService.findAllClients();
        ListIterator<Client> it = clients.listIterator();
        LocalDate now = LocalDate.now();
        while (it.hasNext()){
            Client cl = it.next();
        writer.println(cl.getId() + ";"
                + cl.getPrenom() + ";"
                + cl.getNom() + ";"
                + cl.getDatenaissance().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")) + ";"
                + (now.getYear()-cl.getDatenaissance().getYear()));
        }
        // writer.println("Case00;Case01"); // écrit dans le fichier excel, les cases sont séparées par un ";"
    }

    @GetMapping("/clients/xlsx")
    public void clientsXlsx(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"clients.xlsx\""); // là je lui dis qu'il va faire un fichier à télécharger et qu'il s'appelle clients.xlsx
        LocalDate now = LocalDate.now();
        List<Client> clients = clientService.findAllClients();

        Workbook workbook = new XSSFWorkbook(); // crée un fichier excel
        Sheet sheet = workbook.createSheet("Clients"); // crée un onglet
        Row headerRow = sheet.createRow(0); // crée une ligne
        ArrayList<String> celTitres = new ArrayList<String>() ;
        celTitres.add("Matricule");
        celTitres.add("Prénom");
        celTitres.add("Nom");
        celTitres.add("Date de Naissance");
        celTitres.add("Age");

        int i =0;
        for(String title : celTitres){
            headerRow.createCell(i).setCellValue(title);
            i++;
        }
        //Cell cellPrenom = headerRow.createCell(0); // crée une cellule
        //cellPrenom.setCellValue("Matricule;Prénom;Nom;Date de Naissance"); // ajoute une valeur dans la cellule

        int rowNum = 1;
        for(Client client : clients){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(client.getId());
            row.createCell(1).setCellValue(client.getPrenom());
            row.createCell(2).setCellValue(client.getNom());
            row.createCell(3).setCellValue(client.getDatenaissance().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
            row.createCell(4).setCellValue((now.getYear()-client.getDatenaissance().getYear()));
        }
        workbook.write(response.getOutputStream()); // ici on dit qu'on écrit dans un objet de type Output
        workbook.close(); // ici on dit qu'on ferme l'écriture dans le fichier

    }
    @GetMapping("/factures/xlsx")
    public void facturesXlsx(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"factures.xlsx\"");

        List<Facture> factures = factureService.findAllFacture();

        Workbook workbook = new XSSFWorkbook();

        for(Facture f : factures) {
            Sheet sheet = workbook.createSheet("Facture "+ f.getId());
            Row headerRow = sheet.createRow(0);

            Cell cellHeaderId = headerRow.createCell(0);
            cellHeaderId.setCellValue("Article");

            headerRow.createCell(1).setCellValue("Qté");
            headerRow.createCell(2).setCellValue("Prix Unit.");
            headerRow.createCell(3).setCellValue("Prix Ligne");

            Set<LigneFacture> lignesFacture = f.getLigneFactures();

            int rowNum = 1;
            for (LigneFacture lf : lignesFacture){
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(lf.getArticle().getLibelle());
                row.createCell(1).setCellValue(lf.getQuantite());
                row.createCell(2).setCellValue(lf.getArticle().getPrix());
                row.createCell(3).setCellValue(lf.getArticle().getPrix()*lf.getQuantite());
            }
            Font rowTotalFont = workbook.createFont();
            rowTotalFont.setBold(true);
            rowTotalFont.setColor(IndexedColors.BLUE_GREY.getIndex());

            CellStyle rowTotalStyle = workbook.createCellStyle();
            rowTotalStyle.setFont(rowTotalFont);

            rowTotalStyle.setBorderBottom(BorderStyle.THIN);
            rowTotalStyle.setBorderTop(BorderStyle.THIN);
            rowTotalStyle.setBorderRight(BorderStyle.THIN);
            rowTotalStyle.setBorderLeft(BorderStyle.THIN);

            rowTotalStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            rowTotalStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
            rowTotalStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            rowTotalStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());


            Row rowTotal = sheet.createRow(rowNum++);
            rowTotal.createCell(0).setCellValue("Total facture :");
            rowTotal.createCell(3).setCellValue(f.getTotal());

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();

    }
}
