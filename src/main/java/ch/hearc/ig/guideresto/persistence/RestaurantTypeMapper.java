package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    private final Connection connection;

    public RestaurantTypeMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public RestaurantType findById(int id) {
        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RestaurantType type = new RestaurantType();
                    type.setId(rs.getInt("NUMERO"));
                    type.setLabel(rs.getString("LIBELLE"));
                    type.setDescription(rs.getString("DESCRIPTION"));
                    return type;
                }
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    @Override
    public Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        String sql = "SELECT * FROM TYPES_GASTRONOMIQUES";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RestaurantType type = new RestaurantType();
                type.setId(rs.getInt("NUMERO"));
                type.setLabel(rs.getString("LIBELLE"));
                type.setDescription(rs.getString("DESCRIPTION"));
                types.add(type);
            }

        } catch (SQLException e) {
            System.err.println("Error finding all RestaurantTypes: " + e.getMessage());
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
            e.getMessage();
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
            System.err.println("Error updating RestaurantType: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(RestaurantType type) {
        String sql = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, type.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting RestaurantType: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting RestaurantType by ID: " + e.getMessage());
            return false;
        }
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
