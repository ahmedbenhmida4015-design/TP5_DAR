package rmiClient;

import rmiService.IConversion;
import java.rmi.Naming;

public class ConversionClient {

    public static void main(String[] args) {
        try {

            // Chercher la référence de l'objet distant dans le RMIRegistry
            IConversion stub = (IConversion)
                    Naming.lookup("rmi://localhost:1099/BK");

            // Appel de la méthode distante
            double result = stub.convertirMontant(200);

            // Afficher le résultat
            System.out.println("Résultat de la conversion : " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}