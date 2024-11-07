package ru.mirea.pkmn.gadzhievme;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mirea.pkmn.Card;
import ru.mirea.pkmn.gadzhievme.web.http.PkmnHttpClient;
import ru.mirea.pkmn.gadzhievme.web.jdbc.DatabaseServiceImpl;

import java.util.stream.Collectors;

public class PkmnApplication {
    static String filename = "C:\\IntIdProjects\\Pkmn\\src\\main\\resources\\my_card.txt";
    public static void main(String[] args) throws Exception {
        Card card = CardImport.Import(filename);
        //CardExport.Export(card);
        //Card pok = CardImport.SImport("C:\\IntIdProjects\\Pkmn\\src\\main\\resources\\Pyroar.crd");
        System.out.println(card.toString());
        //System.out.printf(pok.toString());

        PkmnHttpClient pkmnHttpClient = new PkmnHttpClient();

        JsonNode cardHTTP = pkmnHttpClient.getPokemonCard("Pikachu V", "86");
        System.out.println(cardHTTP.toPrettyString());

        DatabaseServiceImpl db = new DatabaseServiceImpl();
        //db.saveCardToDatabase(card);
        Card card1 = db.getCardFromDatabase("Pikachu");
        System.out.println(card1);
    }

}
