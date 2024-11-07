package ru.mirea.pkmn.gadzhievme;

import ru.mirea.pkmn.*;

import java.io.*;
import java.util.ArrayList;


public class CardImport {
    public static Card Import(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        Card card = new Card();
        card.setPokemonStage(PokemonStage.valueOf(reader.readLine()));
        card.setName(reader.readLine());
        card.setHp(Integer.parseInt(reader.readLine()));
        card.setPokemonType(EnergyType.valueOf(reader.readLine()));
        String evo;
        if (!(evo = reader.readLine()).equals("NULL"))
            card.setEvolvesFrom(Import("C:\\IntIdProjects\\Pkmn\\src\\main\\resources\\evolves_from.txt"));
        ArrayList<AttackSkill> attacks = new ArrayList<>();
        for ( String attacksinfo : reader.readLine().split(",")) {
            String[] listOfAttacksInfo = attacksinfo.split("/");
            AttackSkill attackSkill = new AttackSkill(listOfAttacksInfo[1], "", listOfAttacksInfo[0], Integer.parseInt(listOfAttacksInfo[2]));
            attacks.add(attackSkill);
        }
        card.setSkills(attacks);
        card.setWeaknessType(EnergyType.valueOf(reader.readLine()));
        String res;
        if (!(res = reader.readLine()).equals("NULL"))
            card.setResistanceType(EnergyType.valueOf(res));
        card.setRetreatCost(reader.readLine());
        card.setGameSet(reader.readLine());
        card.setRegulationMark(reader.readLine().toCharArray()[0]);
        String[] listOfStudentInfo = reader.readLine().split(" ");
        Student student = new Student(listOfStudentInfo[0], listOfStudentInfo[1], listOfStudentInfo[2], "");
        card.setPokemonOwner(student);
        card.setNumber(reader.readLine());

        reader.close();
        return card;
    }
    public static Card SImport(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        return (Card) objectInputStream.readObject();
    }
}
