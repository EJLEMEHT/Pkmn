package ru.mirea.pkmn.gadzhievme.web.jdbc;

import com.google.gson.reflect.TypeToken;
import ru.mirea.pkmn.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import com.google.gson.Gson;

public class DatabaseServiceImpl implements DatabaseService {

    private static Connection connection = null;

    private final Properties databaseProperties;

    public DatabaseServiceImpl() throws SQLException, IOException {

        // Загружаем файл database.properties

        databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream("C:\\IntIdProjects\\Pkmn\\src\\main\\resources\\database.properties"));

        // Подключаемся к базе данных

        connection = DriverManager.getConnection(
                databaseProperties.getProperty("database.url"),
                databaseProperties.getProperty("database.user"),
                databaseProperties.getProperty("database.password")
        );
        System.out.println("Connection is "+(connection.isValid(0) ? "up" : "down"));
    }

    public static Card getCardFromDatabaseById(UUID uuid) throws SQLException {
        String query = "select * from card WHERE \"id\" = ?";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                Card card = getCardFromResultSet(resultSet);
                return card;
            }
            else return null;
        }
    }

    public static Student getStudentFromDatabaseById(UUID uuid) throws SQLException {
        String query = "select * from student WHERE \"id\" = ?";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                return resultSetToStudent(resultSet);
            }
            else return null;
        }
    }

    @Override
    public Card getCardFromDatabase(String cardName) throws SQLException {
        String query = "select * from card WHERE \"name\" = ?";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cardName);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                Card card = getCardFromResultSet(resultSet);
                card.setNumber(resultSet.getString("card_number"));
                statement.close();
                return card;
            }
            else
                return null;
        }
    }

    @Override
    public Student getStudentFromDatabase(String studentName)
            throws SQLException, IllegalArgumentException {
        String query = "select * from student where \"familyName\" = ? and" +
                " \"firstName\" = ? and" +
                " \"patronicName\" = ?";

        String[] splittedName = studentName.split(" ");
        if (splittedName.length != 3) {
            throw new IllegalArgumentException("You must provide 3 words separated by space in the string!");
        }

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, splittedName[0]);
            statement.setString(2, splittedName[1]);
            statement.setString(3, splittedName[2]);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                return resultSetToStudent(resultSet);
            }
            return null;
        }
    }

    private UUID getCardIdFromDatabase(String cardName) throws SQLException {
        String query = "select * from card WHERE \"name\" = ?";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cardName);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                return UUID.fromString(resultSet.getString("id"));
            }
            return null;
        }
    }

    private UUID getStudentIdFromDatabase(String studentName) throws SQLException {
        String query = "select * from student where \"familyName\" = ? and" + " \"firstName\" = ? and" +
                " \"patronicName\" = ?";

        String[] splittedName = studentName.split(" ");

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, splittedName[0]);
            statement.setString(2, splittedName[1]);
            statement.setString(3, splittedName[2]);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                return UUID.fromString(resultSet.getString("id"));
            }
            return null;
        }
    }

    @Override
    public void saveCardToDatabase(Card card) throws SQLException {
        if(getCardFromDatabase(card.getName()) != null) {
            return;
        }

        Card evolvesFrom;
        UUID evolvesFromId = null;
        if((evolvesFrom = card.getEvolvesFrom()) != null) {
            if(getCardFromDatabase(evolvesFrom.getName()) == null) {
                saveCardToDatabase(evolvesFrom);
            }
            evolvesFromId = getCardIdFromDatabase(evolvesFrom.getName());
        }


        Student pokemonOwner;
        UUID ownerId = null;
        if((pokemonOwner = card.getPokemonOwner()) != null) {
            if(getStudentFromDatabase(
                    pokemonOwner.getSurName() + " " +
                            pokemonOwner.getFirstName() + " " +
                            pokemonOwner.getFamilyName()
            ) == null) {
                createPokemonOwner(card.getPokemonOwner());
            }
            ownerId = getStudentIdFromDatabase(pokemonOwner.getSurName() + " " +
                    pokemonOwner.getFirstName() + " " +
                    pokemonOwner.getFamilyName());
        }


        String query = "insert into card(id, name, hp, evolves_from, " +
                "game_set, pokemon_owner, stage, retreat_cost, " +
                "weakness_type, resistance_type, attack_skills, " +
                "pokemon_type, regulation_mark, card_number) VALUES(" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json, ?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, card.getName());
            statement.setInt(3, card.getHp());
            statement.setObject(4, evolvesFromId);
            statement.setString(5, card.getGameSet());
            statement.setObject(6, ownerId);
            statement.setString(7, card.getPokemonStage().name());
            statement.setString(8, card.getRetreatCost());

            if(card.getWeaknessType() != null) {
                statement.setString(9, card.getWeaknessType().name());
            } else {
                statement.setString(9, null);
            }

            if(card.getResistanceType() != null) {
                statement.setString(10, card.getResistanceType().name());
            } else {
                statement.setString(10, null);
            }

            statement.setString(11, new Gson().toJson(card.getSkills()));
            statement.setString(12, card.getPokemonType().name());
            statement.setString(13, String.valueOf(card.getRegulationMark()));
            statement.setString(14, card.getNumber());

            statement.execute();
        }
    }

    @Override
    public void createPokemonOwner(Student owner) throws SQLException {
        if(getStudentIdFromDatabase(owner.getSurName() + " " +
                owner.getFirstName() + " " + owner.getFamilyName()) != null) {
            return;
        }
        String query = "insert into student(id, " +
                "\"familyName\", \"firstName\", \"patronicName\", \"group\") " +
                "values(?, ?, ?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, owner.getSurName());
            statement.setString(3, owner.getFirstName());
            statement.setString(4, owner.getFamilyName());
            statement.setString(5, owner.getGroup());

            statement.execute();
        }
    }
    public static Card getCardFromResultSet(ResultSet resultSet) throws SQLException {
        Card card = new Card();
        card.setName(resultSet.getString("name"));
        card.setHp(resultSet.getInt("hp"));

        if(resultSet.getString("evolves_from") != null) {
            Card evolvesFromCard = getCardFromDatabaseById(
                    UUID.fromString(resultSet.getString("evolves_from"))
            );
            card.setEvolvesFrom(evolvesFromCard);
        } else {
            card.setEvolvesFrom(null);
        }

        card.setGameSet(resultSet.getString("game_set"));

        if(resultSet.getString("pokemon_owner") != null) {
            card.setPokemonOwner(
                    getStudentFromDatabaseById(
                            UUID.fromString(resultSet.getString("pokemon_owner"))
                    )
            );
        } else
            card.setPokemonOwner(null);


        card.setPokemonStage(PokemonStage.valueOf(resultSet.getString("stage")));

        String value;
        if((value = resultSet.getString("retreat_cost")) != null) {
            card.setRetreatCost(value);
        } else card.setRetreatCost(null);

        if((value = resultSet.getString("weakness_type")) != null) {
            card.setWeaknessType(EnergyType.valueOf(value));
        } else card.setWeaknessType(null);

        if((value = resultSet.getString("resistance_type")) != null) {
            card.setResistanceType(EnergyType.valueOf(value));
        } else card.setResistanceType(null);

        Gson gson = new Gson();
        Type type = new TypeToken<List<AttackSkill>>() {}.getType();
        List<AttackSkill> skills = gson.fromJson(resultSet.getString("attack_skills"), type);
        card.setSkills(skills);

        card.setPokemonType(EnergyType.valueOf(resultSet.getString("pokemon_type")));

        if((value = resultSet.getString("regulation_mark")) != null) {
            card.setRegulationMark(value.charAt(0));
        } else card.setRegulationMark(null);

        card.setNumber(resultSet.getString("card_number"));
        return card;
    }

    public static Student resultSetToStudent(ResultSet resultSet) throws SQLException {
        Student student = new Student();

        String value;
        if((value = resultSet.getString("firstName")) != null) {
            student.setFirstName(value);
        } else student.setFirstName(null);

        if((value = resultSet.getString("familyName")) != null) {
            student.setSurName(value);
        } else student.setSurName(null);

        if((value = resultSet.getString("patronicName")) != null) {
            student.setFamilyName(value);
        } else student.setFamilyName(null);

        if((value = resultSet.getString("group")) != null) {
            student.setGroup(value);
        } else student.setGroup(null);

        return student;
    }
}
