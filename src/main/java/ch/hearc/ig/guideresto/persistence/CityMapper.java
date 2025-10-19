package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


public class CityMapper extends AbstractMapper<City>{

    private final Connection connection;

    private Map<Long, City> cityCache = new HashMap<>();//identity map

    private City addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");

        if (!this.cityCache.containsKey((long) id)) {
            System.out.println("[CACHE MISS] Ville " + id + " créé depuis DB");
            City city = new City();
            city.setId(rs.getInt("NUMERO"));
            city.setCityName(rs.getString("NOM_VILLE"));
            city.setZipCode(rs.getString("CODE_POSTAL"));

            cityCache.put((long) id, city);
        } else {
            System.out.println("[CACHE HIT] Ville " + id + " récupéré depuis cache");
        }
        return this.cityCache.get((long) id);
    }


    public CityMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public City findById(int id) {
        if (this.cityCache.containsKey(id)) {
            System.out.println("[CACHE HIT BEFORE QUERY] Restaurant " + id);
            return this.cityCache.get(id);
        }
        String sql = "SELECT * FROM VILLES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[CACHE MISS BEFORE QUERY] Restaurant " + id + " → requête SQL");
                    City city = addToCache(rs);
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
        String sql = "SELECT * FROM VILLES";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                City city = addToCache(rs);
                cities.add(city);
            }

        } catch (SQLException e) {
            e.getMessage();
        }
        return cities;
    }

    @Override
    public City create(City city) {
        String sql = "INSERT INTO VILLES (CODE_POSTAL, NOM_VILLE) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, new String[] {"NUMERO"})) {
            ps.setString(1, city.getZipCode());
            ps.setString(2, city.getCityName());

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
        String sql = "UPDATE VILLES SET NOM_VILLE = ?, CODE_POSTAL = ? WHERE NUMERO = ?";
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
        String sql = "DELETE FROM VILLES WHERE NUMERO = ?";

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
        String sql = "DELETE FROM VILLES WHERE NUMERO = ?";
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
        return "SELECT SEQ_VILLES.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT COUNT(*) FROM VILLES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM VILLES";
    }
}
