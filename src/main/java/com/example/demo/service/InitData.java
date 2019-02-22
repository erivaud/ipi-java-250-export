package com.example.demo.service;

import com.example.demo.entity.Article;
import com.example.demo.entity.Client;
import com.example.demo.entity.Facture;
import com.example.demo.entity.LigneFacture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;

/**
 * Classe permettant d'insérer des données dans l'application.
 */
@Service
@Transactional
public class InitData implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private EntityManager em;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        insertTestData();
    }

    private void insertTestData() {

        Client client1 = newClient("PETRILLO", "Alexandre", LocalDate.of(1983, 12, 19));
        em.persist(client1);

        Client client2 = newClient("Dupont", "Jérome", LocalDate.of(2008, 03, 25));
        em.persist(client2);

        Client client3 = newClient("Rivaud", "Estelle", LocalDate.of(1982, 04, 13));
        em.persist(client3);

        Article article1 = new Article();
        article1.setLibelle("Ordi");
        article1.setPrix(350);
        em.persist(article1);

        Article article2 = new Article();
        article2.setLibelle("Macintosh");
        article2.setPrix(850);
        em.persist(article2);

        Facture f1 = new Facture();
        f1.setClient(client1);
        em.persist(f1);
        em.persist(newLigneFacture(f1, article1, 2));
        em.persist(newLigneFacture(f1, article2, 1));

        Facture f2 = new Facture();
        f2.setClient(client2);
        em.persist(f2);
        em.persist(newLigneFacture(f2, article1, 10));
    }

    private LigneFacture newLigneFacture(Facture f, Article a1, int quantite) {
        LigneFacture lf1 = new LigneFacture();
        lf1.setArticle(a1);
        lf1.setQuantite(quantite);
        lf1.setFacture(f);
        return lf1;
    }








    private Client newClient(String nom, String prenom, LocalDate date) {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setDatenaissance(date);
        return client;
    }
}
