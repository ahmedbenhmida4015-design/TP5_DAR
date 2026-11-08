package rmiClient;

import metier.Compte;
import rmiService.IBanque;

import java.rmi.Naming;

public class BanqueClient {

    public static void main(String[] args) {

        try {

            IBanque banque = (IBanque)
                    Naming.lookup(
                            "rmi://localhost:1099/Banque"
                    );

            // Création d'un compte
            Compte c1 = new Compte(1001, 5000);

            String resultat = banque.creerCompte(c1);

            System.out.println(resultat);

            // Consultation du compte
            String info = banque.getInfoCompte(1001);

            System.out.println(info);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}