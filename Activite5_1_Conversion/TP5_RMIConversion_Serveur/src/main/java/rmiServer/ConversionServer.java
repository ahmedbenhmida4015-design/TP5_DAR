package rmiServer;

import rmiService.ConversionImpl;

import javax.naming.InitialContext;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ConversionServer {

    static void main(String[] args) {

        try {
            // i. Activer l'annuaire RMIRegistry (port 1099)
            LocateRegistry.createRegistry(1099);

            // ii. Créer une instance de ConversionImpl
            ConversionImpl od = new ConversionImpl();
            System.out.println(od.toString());

            // iii. Publier la référence dans l'annuaire
            Naming.rebind("rmi://localhost:1099/BK", od);
            System.out.println("Serveur RMI de conversion démarré...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
