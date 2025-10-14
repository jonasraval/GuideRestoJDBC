package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class CityMapper extends AbstractMapper<City>{

    private final Connection connection;

    public CityMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public City findById(int id) {
        String sql = "SELECT * FROM VILLE WHERE ID_VILLE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    City city = new City();
                    city.setId(rs.getInt("ID_VILLE"));
                    city.setCityName(rs.getString("NOM"));
                    city.setZipCode(rs.getString("CODE_POSTAL"));
                    return city;
                }
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    @Override
    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        String sql = "SELECT * FROM VILLE";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                City city = new City();
                city.setId(rs.getInt("ID_VILLE"));
                city.setCityName(rs.getString("NOM"));
                city.setZipCode(rs.getString("CODE_POSTAL"));
                cities.add(city);
            }

        } catch (SQLException e) {
            e.getMessage();
        }
        return cities;
    }

    @Override
    public City create(City city) {
        String sql = "INSERT INTO VILLE (NOM, CODE_POSTAL) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, city.getCityName());
            ps.setString(2, city.getZipCode());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    city.setId(rs.getInt(1));
                }
            }

            return city;

        } catch (SQLException e) {
            System.err.println("Error creating city: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(City city) {
        String sql = "UPDATE VILLE SET NOM = ?, CODE_POSTAL = ? WHERE ID_VILLE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, city.getCityName());
            ps.setString(2, city.getZipCode());
            ps.setInt(3, city.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating city: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(City city) {
        String sql = "DELETE FROM VILLE WHERE ID_VILLE = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, city.getId());
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.getMessage();
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM VILLE WHERE ID_VILLE = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting city: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT VILLE_SEQ.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT COUNT(*) FROM VILLE WHERE ID_VILLE = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM VILLE";
    }
}
