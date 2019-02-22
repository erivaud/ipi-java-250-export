package com.example.demo.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Alexandre on 09/04/2018.
 */
@Entity
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "facture")
    private Set<LigneFacture> ligneFactures;

    @ManyToOne
    private Client client;

    public double getTotal(){
        double total =0;
        for(LigneFacture ligne : ligneFactures){
           total += ligne.getQuantite()* ligne.getArticle().getPrix();
        }
        return total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<LigneFacture> getLigneFactures() {
        return ligneFactures;
    }

    public void setLigneFactures(Set<LigneFacture> lignes) {
        this.ligneFactures = lignes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
