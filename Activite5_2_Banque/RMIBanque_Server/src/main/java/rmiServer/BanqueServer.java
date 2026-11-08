package rmiServer;

import rmiService.BanqueImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

public class BanqueServer {

    public static void main(String[] args) {

        try {
            LocateRegistry.createRegistry(1099);

            BanqueImpl banque = new BanqueImpl();

            Naming.rebind(
                    "rmi://localhost:1099/Banque",
                    banque
            );

            System.out.println(
                    "Serveur Banque démarré avec succès !"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}