package com.example.demo.controller;

import java.io.FileOutputStream;
import java.io.OutputStream;
import com.example.demo.entity.Client;
import com.example.demo.entity.Facture;
import com.example.demo.entity.LigneFacture;
import com.example.demo.service.ClientService;
import com.example.demo.service.FactureService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.*;

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

        List<Client> clients = clientService.findAllClients();
        List<Facture> factures = factureService.findAllFacture();

        Workbook workbook = new XSSFWorkbook();
        Map<String, CellStyle> styles = createStyles(workbook);
        for (Client c : clients) {
            Sheet sheetClient = workbook.createSheet( c.getPrenom() + " " + c.getNom());
            Row rowClient = sheetClient.createRow(0);
            rowClient.createCell(0).setCellValue(c.getPrenom() + " " + c.getNom());
            rowClient.getCell(0).setCellStyle(styles.get("TotalHeader"));
            sheetClient.autoSizeColumn(0);

            for (Facture f : factures) {
                if(c.getId() == f.getClient().getId()) {
                    Sheet sheet = workbook.createSheet("Facture " + f.getId());

                    Row headerRow = sheet.createRow(0);
                    headerRow.setHeightInPoints(30);

                    Cell cellHeaderId = headerRow.createCell(0);
                    cellHeaderId.setCellValue("Article");

                    headerRow.createCell(1).setCellValue("Qté");
                    headerRow.createCell(2).setCellValue("Prix Unit.");
                    headerRow.createCell(3).setCellValue("Prix Ligne");

                    Set<LigneFacture> lignesFacture = f.getLigneFactures();

                    int rowNum = 1;
                    for (LigneFacture lf : lignesFacture) {
                        Row row = sheet.createRow(rowNum++);
                        row.setHeightInPoints(30);
                        row.createCell(0).setCellValue(lf.getArticle().getLibelle());
                        row.createCell(1).setCellValue(lf.getQuantite());
                        row.createCell(2).setCellValue(lf.getArticle().getPrix());
                        row.createCell(3).setCellValue(lf.getArticle().getPrix() * lf.getQuantite());
                        for (int i = 0; i < 4; i++) {
                            row.getCell(i).setCellStyle(styles.get("Normal"));
                        }
                        row.getCell(1).setCellStyle(styles.get("Centered"));
                    }


                    Row totalRow = sheet.createRow(rowNum++);
                    totalRow.setHeightInPoints(30);
                    totalRow.createCell(0).setCellValue("Total facture :");
                    totalRow.createCell(1);
                    totalRow.createCell(2);

                    CellRangeAddress cellRangeAddress = new CellRangeAddress(
                            totalRow.getRowNum(), totalRow.getRowNum(),
                            totalRow.getFirstCellNum(), (totalRow.getFirstCellNum() + 2));
                    sheet.addMergedRegion(cellRangeAddress);
                    totalRow.createCell(3).setCellValue(f.getTotal());


                    for (int i = 0; i < 4; i++) {
                        sheet.setColumnWidth(i, 350 * 15);

                        headerRow.getCell(i).setCellStyle(styles.get("Header"));

                        if (totalRow.getCell(i) != null & i < 3) {
                            totalRow.getCell(i).setCellStyle(styles.get("TotalHeader"));
                        } else if (totalRow.getCell(i) != null & i == 3) {
                            totalRow.getCell(i).setCellStyle(styles.get("Total"));
                        }

                    }
                }
            }
        }
        workbook.write(response.getOutputStream());
        workbook.close();


    }
        /**
         * Styling cells & columns starts here
         */
        private static Map<String, CellStyle> createStyles(Workbook workbook){

            Map<String, CellStyle> styles = new HashMap<>();

            CellStyle cellStyleNormal = workbook.createCellStyle();
            Font fontNormal = workbook.createFont();
            fontNormal.setFontHeightInPoints((short)13);
            cellStyleNormal.setFont(fontNormal);
            cellStyleNormal.setVerticalAlignment(VerticalAlignment.CENTER);
            styles.put("Normal", cellStyleNormal);

            CellStyle cellStyleCentered = workbook.createCellStyle();
            cellStyleCentered.setAlignment(HorizontalAlignment.CENTER);
            cellStyleCentered.setVerticalAlignment(VerticalAlignment.CENTER);
            styles.put("Centered", cellStyleCentered);

            CellStyle cellStyleHeader = workbook.createCellStyle();
            Font fontHeader = workbook.createFont();
            fontHeader.setFontHeightInPoints((short)18);
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            cellStyleHeader.setFont(fontHeader);
            cellStyleHeader.setAlignment(HorizontalAlignment.CENTER);
            cellStyleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyleHeader.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            cellStyleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            styles.put("Header", cellStyleHeader);

            CellStyle cellStyleTotal = workbook.createCellStyle();
            Font fontTotal = workbook.createFont();
            fontTotal.setFontHeightInPoints((short)20);
            fontTotal.setBold(true);
            fontTotal.setColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotal.setFont(fontTotal);

            cellStyleTotal.setTopBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotal.setRightBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotal.setBottomBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotal.setLeftBorderColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotal.setBorderTop(BorderStyle.THIN);
            cellStyleTotal.setBorderRight(BorderStyle.THIN);
            cellStyleTotal.setBorderBottom(BorderStyle.THIN);
            cellStyleTotal.setBorderLeft(BorderStyle.THIN);
            cellStyleTotal.setAlignment(HorizontalAlignment.CENTER);
            cellStyleTotal.setVerticalAlignment(VerticalAlignment.CENTER);

            styles.put("Total", cellStyleTotal);

            CellStyle cellStyleTotalH = workbook.createCellStyle();
            Font fontTotalH = workbook.createFont();
            fontTotalH.setFontHeightInPoints((short)20);
            fontTotalH.setBold(true);
            fontTotalH.setColor(IndexedColors.WHITE.getIndex());
            cellStyleTotalH.setFont(fontTotalH);

            cellStyleTotalH.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellStyleTotalH.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            styles.put("TotalHeader", cellStyleTotalH);

            return styles;
        }

}
