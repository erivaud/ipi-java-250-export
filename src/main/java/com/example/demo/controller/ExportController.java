package com.example.demo.controller;

import com.example.demo.entity.Client;
import com.example.demo.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;

/**
 * Controlleur pour réaliser les exports.
 */
@Controller
@RequestMapping("/")
public class ExportController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/clients/csv")
    public void clientsCSV(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"clients.csv\""); // là je lui dis qu'il va faire un fichier à télécharger et qu'il s'appelle clients.csv
        PrintWriter writer = response.getWriter(); // je récupère le writter de la réponse Http
        // TODO
        List<Client> clients = clientService.findAllClients();
        ListIterator<Client> it = clients.listIterator();
        while (it.hasNext()){
            Client cl = it.next();

        writer.println(cl.getId() + ";" +cl.getPrenom() + ";" + cl.getNom());
        }
        // writer.println("Case00;Case01"); // écrit dans le fichier excel, les cases sont séparées par un ";"
       // writer.println("Case10;Case11");
    }

}
