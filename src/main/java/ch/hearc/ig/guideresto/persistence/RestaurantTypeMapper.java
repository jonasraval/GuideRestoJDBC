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

    private final Connection connection;

    public RestaurantTypeMapper(Connection connection) {
        this.connection = connection;
    }

    private Map<Integer, RestaurantType> typeCache = new HashMap<>();

    private RestaurantType addToCache(ResultSet rs) throws SQLException {
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
        if (typeCache.containsKey(id)) {
            return typeCache.get(id);
        }
        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RestaurantType type = addToCache(rs);
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
        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RestaurantType type = addToCache(rs);

                types.add(type);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return types;
    }

    @Override
    public RestaurantType create(RestaurantType type) {
        String sql = "INSERT INTO TYPES_GASTRONOMIQUES (LIBELLE, DESCRIPTION) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
        String sql = "UPDATE TYPES_GASTRONOMIQUES SET LIBELLE = ?, DESCRIPTION = ? WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        return "SELECT TYPES_GASTRONOMIQUES_SEQ.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM TYPES_GASTRONOMIQUES";
    }
}
