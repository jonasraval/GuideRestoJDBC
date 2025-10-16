package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private final Connection connection;

    private final RestaurantTypeMapper restaurantTypeMapper;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final CityMapper cityMapper;

    public RestaurantMapper(Connection connection) {
        this.connection = connection;
        this.restaurantTypeMapper = new RestaurantTypeMapper(connection);
        this.completeEvaluationMapper = new CompleteEvaluationMapper(connection);
        this.cityMapper = new CityMapper(connection);
    }



    @Override
    public Restaurant findById(int id) {
        String sql= "SELECT * FROM restaurant WHERE numero = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setId(rs.getInt("NUMERO"));
                    restaurant.setName(rs.getString("NOM"));
                    restaurant.setDescription(rs.getString("DESCRIPTION"));
                    restaurant.setWebsite(rs.getString("WEBSITE"));

                    String adresse = rs.getString("ADRESSE");
                    int cityId = rs.getInt("FK_VILL");
                    City city = cityMapper.findById(cityId);
                    Localisation localisation = new Localisation(adresse, city);
                    restaurant.setAddress(localisation);


                    int typeId = rs.getInt("TYPE_ID");
                    RestaurantType restaurantType = restaurantTypeMapper.findById(typeId);
                    restaurant.setType(restaurantType);

                    Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurantId(restaurant.getId());
                    Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                    restaurant.setEvaluations(evaluations);

                    return restaurant;
                }

            }
        } catch (SQLException ex){
            System.err.println(ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<Restaurant> findAll() {
        Set<Restaurant> restaurantSet = new HashSet<>();
        String sql = "SELECT * FROM RESTAURANTS";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Restaurant restaurant = new Restaurant();

                restaurant.setId(rs.getInt("NUMERO"));
                restaurant.setName(rs.getString("NOM"));
                restaurant.setDescription(rs.getString("DESCRIPTION"));
                restaurant.setWebsite(rs.getString("WEBSITE"));

                String adresse = rs.getString("ADRESSE");
                int cityId = rs.getInt("FK_VILL");
                City city = cityMapper.findById(cityId);
                Localisation localisation = new Localisation(adresse, city);
                restaurant.setAddress(localisation);

                int typeId = rs.getInt("TYPE_ID");
                RestaurantType restaurantType = restaurantTypeMapper.findById(typeId);
                restaurant.setType(restaurantType);

                Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurantId(restaurant.getId());
                Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                restaurant.setEvaluations(evaluations);

                restaurantSet.add(restaurant);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return restaurantSet;
    }

    public Set<Restaurant> findByCity(String cityName) {
        Set<Restaurant> restaurantSet = new HashSet<>();
        String sql = """
        SELECT r.*
        FROM RESTAURANTS r
        JOIN VILLES v ON r.FK_VILL = v.NUMERO
        WHERE LOWER(v.NOM_VILLE) LIKE LOWER(?)
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + cityName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = new Restaurant();

                    restaurant.setId(rs.getInt("NUMERO"));
                    restaurant.setName(rs.getString("NOM"));
                    restaurant.setDescription(rs.getString("DESCRIPTION"));
                    restaurant.setWebsite(rs.getString("WEBSITE"));

                    String adresse = rs.getString("ADRESSE");
                    int cityId = rs.getInt("FK_VILL");
                    City city = cityMapper.findById(cityId);
                    Localisation localisation = new Localisation(adresse, city);
                    restaurant.setAddress(localisation);

                    int typeId = rs.getInt("TYPE_ID");
                    RestaurantType restaurantType = restaurantTypeMapper.findById(typeId);
                    restaurant.setType(restaurantType);

                    Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurantId(restaurant.getId());
                    Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                    restaurant.setEvaluations(evaluations);

                    restaurantSet.add(restaurant);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error finding restaurants in city: " + cityName, ex);
        }

        return restaurantSet;
    }

    @Override
    public Restaurant create(Restaurant restaurant) {
        String insertSql = "INSERT INTO RESTAURANT (NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, restaurant.getName());
            ps.setString(2, restaurant.getAddress().getStreet());
            ps.setString(3, restaurant.getDescription());
            ps.setString(4, restaurant.getWebsite());
            ps.setInt(5, restaurant.getType().getId());
            ps.setInt(6, restaurant.getAddress().getCity().getId());

            ps.executeUpdate();

            try(ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    restaurant.setId(generatedId);
                }
            }

            return restaurant;

            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Restaurant restaurant) {
        String sql = "UPDATE RESTAURANT " +
                "SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ?, FK_VILL = ? " +
                "WHERE NUMERO = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, restaurant.getName());
            ps.setString(2, restaurant.getAddress().getStreet());
            ps.setString(3, restaurant.getDescription());
            ps.setString(4, restaurant.getWebsite());
            ps.setInt(5, restaurant.getType().getId());
            ps.setInt(6, restaurant.getAddress().getCity().getId());
            ps.setInt(7, restaurant.getId());

            int rows = ps.executeUpdate();
            return rows > 0; // true si au moins une ligne mise à jour

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du restaurant : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Restaurant restaurant) {
        String sql = "DELETE FROM RESTAURANT WHERE NUMERO = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, restaurant.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.getMessage();
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM RESTAURANT WHERE NUMERO = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.getMessage();
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT RESTAURANT_SEQ.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT COUNT(*) FROM RESTAURANT WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM RESTAURANT";
    }
}
