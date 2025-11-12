package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


public class CityMapper extends AbstractMapper<City>{
    private static final String SELECT_BY_ID = "SELECT * FROM VILLES WHERE NUMERO = ?";
    private static final String SELECT_ALL = "SELECT * FROM VILLES";
    private static final String INSERT_QUERY = "INSERT INTO VILLES (CODE_POSTAL, NOM_VILLE) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE VILLES SET NOM_VILLE = ?, CODE_POSTAL = ? WHERE NUMERO = ?";
    private static final String DELETE_QUERY = "DELETE FROM VILLES WHERE NUMERO = ?";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_VILLES.NEXTVAL FROM DUAL";
    private static final String EXISTS_QUERY = "SELECT COUNT(*) FROM VILLES WHERE NUMERO = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM VILLES";

    private final Connection connection;

    private City mapRow(ResultSet rs) throws SQLException {
        City city = new City();
        city.setId(rs.getInt("NUMERO"));
        city.setCityName(rs.getString("NOM_VILLE"));
        city.setZipCode(rs.getString("CODE_POSTAL"));

        addToCache(city);

        return city;
    }


    public CityMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public City findById(int id) {
        City cacheCity = getFromCache(id);
        if (cacheCity != null) return cacheCity;
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur : "+e.getMessage());
        }
        return null;
    }

    @Override
    public Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                City city = mapRow(rs);
                cities.add(city);
            }

        } catch (SQLException e) {
            System.out.println("Erreur : "+e.getMessage());
        }
        return cities;
    }

    @Override
    public City create(City city) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY , new String[] {"NUMERO"})) {
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
            System.err.println("Erreur création de ville: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(City city) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY )) {
            ps.setString(1, city.getCityName());
            ps.setString(2, city.getZipCode());
            ps.setInt(3, city.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour de la ville: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(City city) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY )) {
            ps.setInt(1, city.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                removeFromCache(city.getId());
                return true;
            }
        } catch (SQLException e) {
            e.getMessage();
            return false;
        }
        return false;
    }

    @Override
    public boolean deleteById(int id) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY )) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                removeFromCache(id);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur de suppression de la ville: " + e.getMessage());
            return false;
        }
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
