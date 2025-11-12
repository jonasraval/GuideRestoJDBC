package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM TYPES_GASTRONOMIQUES";
    private static final String INSERT_QUERY = "INSERT INTO TYPES_GASTRONOMIQUES (LIBELLE, DESCRIPTION) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE TYPES_GASTRONOMIQUES SET LIBELLE = ?, DESCRIPTION = ? WHERE NUMERO = ?";
    private static final String DELETE_QUERY = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
    private static final String SEQUENCE_QUERY = "SELECT TYPES_GASTRONOMIQUES_SEQ.NEXTVAL FROM DUAL";
    private static final String EXISTS_QUERY = "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";

    private final Connection connection;

    public RestaurantTypeMapper(Connection connection) {
        this.connection = connection;
    }

    private RestaurantType mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");

            RestaurantType type = new RestaurantType();
            type.setId(id);
            type.setLabel(rs.getString("LIBELLE"));
            type.setDescription(rs.getString("DESCRIPTION"));

            addToCache(type);
        return type;
    }

    @Override
    public RestaurantType findById(int id) {
        RestaurantType cacheRestaurantType = getFromCache(id);
        if (!isCacheEmpty()) return cacheRestaurantType;
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_QUERY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RestaurantType type = mapRow(rs);
                    return type;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur : "+ e.getMessage());
        }
        return null;
    }

    @Override
    public Set<RestaurantType> findAll() {
        resetCache();
        Set<RestaurantType> types = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RestaurantType type = mapRow(rs);

                types.add(type);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return types;
    }

    @Override
    public RestaurantType create(RestaurantType type) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.getLabel());
            ps.setString(2, type.getDescription());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    type.setId(rs.getInt(1));
                }
            }

            return type;

        } catch (SQLException e) {
            System.out.println("Erreur : "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(RestaurantType type) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, type.getLabel());
            ps.setString(2, type.getDescription());
            ps.setInt(3, type.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(RestaurantType type) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setInt(1, type.getId());
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                removeFromCache(type.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
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
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    protected String getSequenceQuery() {
        return SEQUENCE_QUERY ;
    }

    @Override
    protected String getExistsQuery() {
        return EXISTS_QUERY ;
    }

    @Override
    protected String getCountQuery() {
        return COUNT_QUERY ;
    }
}
