package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM restaurants WHERE numero = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM RESTAURANTS";
    private static final String FIND_BY_CITY_QUERY = """
        SELECT r.*
        FROM RESTAURANTS r
        JOIN VILLES v ON r.FK_VILL = v.NUMERO
        WHERE LOWER(v.NOM_VILLE) LIKE LOWER(?)
    """;
    private static final String FIND_BY_TYPE_QUERY = "SELECT * FROM RESTAURANTS WHERE FK_TYPE = ?";
    private static final String FIND_BY_NAME_QUERY = "SELECT * FROM RESTAURANTS WHERE LOWER(NOM) LIKE LOWER(?)";
    private static final String INSERT_QUERY = "INSERT INTO restaurants (NUMERO, NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL) VALUES (SEQ_RESTAURANTS.NEXTVAL, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE restaurants SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ?, FK_VILL = ? WHERE NUMERO = ?";
    private static final String DELETE_QUERY = "DELETE FROM restaurants WHERE NUMERO = ?";
    private static final String SEQUENCE_QUERY = "SELECT RESTAURANTS_SEQ.NEXTVAL FROM DUAL";
    private static final String EXISTS_QUERY = "SELECT COUNT(*) FROM restaurants WHERE NUMERO = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM restaurants";

    private final Connection connection;
    private RestaurantTypeMapper restaurantTypeMapper;
    private CompleteEvaluationMapper completeEvaluationMapper;
    private CityMapper cityMapper;
    private BasicEvaluationMapper basicEvaluationMapper;


    private Restaurant mapRow(ResultSet rs) throws SQLException {
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
        Restaurant cacheRestaurant = getFromCache(id);
        if (cacheRestaurant != null) return cacheRestaurant;
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_QUERY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Restaurant restaurant = mapRow(rs);

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
        Set<Restaurant> restaurantSet = new HashSet<>();

        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Restaurant restaurant = mapRow(rs);

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
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_CITY_QUERY)) {
            ps.setString(1, "%" + cityName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapRow(rs);

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
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_TYPE_QUERY)) {
            ps.setInt(1, typeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapRow(rs);

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


        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_NAME_QUERY)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = mapRow(rs);

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
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"NUMERO"})) {
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
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
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
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
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
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
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
        return SEQUENCE_QUERY;
    }

    @Override
    protected String getExistsQuery() {
        return EXISTS_QUERY;
    }

    @Override
    protected String getCountQuery() {
        return COUNT_QUERY;
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
