package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private final Connection connection;

    private RestaurantTypeMapper restaurantTypeMapper;
    private CompleteEvaluationMapper completeEvaluationMapper;
    private CityMapper cityMapper;
    private BasicEvaluationMapper basicEvaluationMapper;

    private Map<Integer, Restaurant> restaurantsCache = new HashMap<>(); //identity map

    private Restaurant addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");
            Restaurant restaurant = new Restaurant();
            restaurant.setId(id);
            restaurant.setName(rs.getString("NOM"));
            restaurant.setDescription(rs.getString("DESCRIPTION"));
            restaurant.setWebsite(rs.getString("SITE_WEB"));

            String adresse = rs.getString("ADRESSE");
            int cityId = rs.getInt("FK_VILL");
            City city = cityMapper.findById(cityId);
            Localisation localisation = new Localisation(adresse, city);
            restaurant.setAddress(localisation);

            int typeId = rs.getInt("FK_TYPE");
            RestaurantType restaurantType = restaurantTypeMapper.findById(typeId);
            restaurant.setType(restaurantType);

        if (completeEvaluationMapper != null) {
            Set<CompleteEvaluation> completeEvals = completeEvaluationMapper.findByRestaurant(restaurant);
            restaurant.getEvaluations().addAll(completeEvals);
        }

            addToCache(restaurant);
        return restaurant;
    }

    public RestaurantMapper(Connection connection) {
        this.connection = connection;
    }



    @Override
    public Restaurant findById(int id) {
        if (restaurantsCache.containsKey(id)) {
            return restaurantsCache.get(id);
        }
        String sql= "SELECT * FROM restaurants WHERE numero = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Restaurant restaurant = addToCache(rs);

                    Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurant(restaurant);
                    Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                    restaurant.setEvaluations(evaluations);

                    return restaurant;
                }

            }
        } catch (SQLException ex){
            System.err.println("Erreur : "+ex.getMessage());
        }
        return null;
    }

    @Override
    public Set<Restaurant> findAll() {
        resetCache();
        Set<Restaurant> restaurantSet = new HashSet<>();
        String sql = "SELECT * FROM RESTAURANTS";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Restaurant restaurant = addToCache(rs);

                if (restaurant.getEvaluations() == null) {
                    Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurant(restaurant);
                    Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                    restaurant.setEvaluations(evaluations);
                }
                restaurantSet.add(restaurant);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur : "+ex.getMessage());
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
                    Restaurant restaurant = addToCache(rs);

                    if (restaurant.getEvaluations() == null) {
                        Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurant(restaurant);
                        Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                        restaurant.setEvaluations(evaluations);
                    }

                    restaurantSet.add(restaurant);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur : " + ex.getMessage());
        }

        return restaurantSet;
    }


    public Set<Restaurant> findByType(int typeId) {
        Set<Restaurant> restaurantSet = new HashSet<>();
        String sql = "SELECT * FROM RESTAURANTS WHERE FK_TYPE = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, typeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = addToCache(rs);

                    if (restaurant.getEvaluations() == null) {
                        Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurant(restaurant);
                        Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                        restaurant.setEvaluations(evaluations);
                    }

                    restaurantSet.add(restaurant);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la recherche des restaurants pour le type ID " + ex.getMessage());
        }

        return restaurantSet;
    }

    public Set<Restaurant> findByName(String name) {
        Set<Restaurant> restaurantSet = new HashSet<>();
        String sql = "SELECT * FROM RESTAURANTS WHERE LOWER(NOM) LIKE LOWER(?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = addToCache(rs);

                    if (restaurant.getEvaluations() == null) {
                        Set<CompleteEvaluation> completeEvaluations = completeEvaluationMapper.findByRestaurant(restaurant);
                        Set<Evaluation> evaluations = new HashSet<>(completeEvaluations);
                        restaurant.setEvaluations(evaluations);
                    }

                    restaurantSet.add(restaurant);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la recherche de restaurants par nom : " + ex.getMessage());
        }

        return restaurantSet;
    }




    @Override
    public Restaurant create(Restaurant restaurant) {
        String insertSql = "INSERT INTO restaurants (NUMERO, NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL) " +
                "VALUES (SEQ_RESTAURANTS.NEXTVAL, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"NUMERO"})) {
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
        String sql = "UPDATE restaurants SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ?, FK_VILL = ? WHERE NUMERO = ?";

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
        String sql = "DELETE FROM restaurants WHERE NUMERO = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, restaurant.getId());
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                removeFromCache(restaurant.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM restaurants WHERE NUMERO = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                removeFromCache(id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT RESTAURANTS_SEQ.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT COUNT(*) FROM restaurants WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM restaurants";
    }

    public void setCompleteEvaluationMapper(CompleteEvaluationMapper completeEvaluationMapper) {
        this.completeEvaluationMapper = completeEvaluationMapper;
    }

    public void setCityMapper(CityMapper cityMapper) {
        this.cityMapper = cityMapper;
    }

    public void setRestaurantTypeMapper(RestaurantTypeMapper restaurantTypeMapper) {
        this.restaurantTypeMapper = restaurantTypeMapper;
    }
}
