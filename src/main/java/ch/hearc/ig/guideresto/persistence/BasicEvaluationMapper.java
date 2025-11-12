package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;

import java.sql.*;
import java.util.*;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation>{
    private static final String SELECT_BY_ID_QUERY = "SELECT * FROM LIKES WHERE NUMERO = ?";
    private static final String SELECT_ALL_QUERY = "SELECT * FROM LIKES";
    private static final String COUNT_BY_RESTAURANT_QUERY = "SELECT COUNT(*) FROM LIKES WHERE FK_REST = ? AND APPRECIATION = ?";
    private static final String INSERT_QUERY = "INSERT INTO LIKES (NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST) VALUES (SEQ_EVAL.NEXTVAL, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE LIKES SET APPRECIATION=?, DATE_EVAL=?, ADRESSE_IP=?, FK_REST=? WHERE NUMERO=?";
    private static final String DELETE_QUERY = "DELETE FROM LIKES WHERE NUMERO=?";
    private static final String EXISTS_QUERY = "SELECT 1 FROM LIKES WHERE NUMERO=?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM LIKES";
    private static final String SEQUENCE_QUERY = "SELECT SEQ_EVAL.NEXTVAL FROM dual";


    private final Connection connection;
    private RestaurantMapper restaurantMapper;

    public BasicEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    public void setRestaurantMapper(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    private BasicEvaluation mapRow(ResultSet rs) throws SQLException{
        int id = rs.getInt("NUMERO");
        BasicEvaluation basicEvaluation = new BasicEvaluation();
        basicEvaluation.setId(id);
        basicEvaluation.setIpAddress(rs.getString("ADRESSE_IP"));
        basicEvaluation.setVisitDate(rs.getDate("DATE_EVAL"));
        basicEvaluation.setLikeRestaurant("Y".equalsIgnoreCase(rs.getString("APPRECIATION")));

        int restaurantId = rs.getInt("FK_REST");

        if (restaurantMapper != null) {
            basicEvaluation.setRestaurant(restaurantMapper.findById(restaurantId));
        }

        addToCache(basicEvaluation);
        return basicEvaluation;
    }

    @Override
    public BasicEvaluation findById(int id) {
        BasicEvaluation cacheBasicEvaluation = getFromCache(id);
        if (cacheBasicEvaluation != null) return cacheBasicEvaluation;

        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
        return null;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> basicEvaluationSet = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                basicEvaluationSet.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
        return basicEvaluationSet;
    }

    public int countLikesForRestaurant(int restaurantId, boolean like) {
        try (PreparedStatement ps = connection.prepareStatement(COUNT_BY_RESTAURANT_QUERY)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, like ? "Y" : "N");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
    }


    @Override
    public BasicEvaluation create(BasicEvaluation eval) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY )) {
            ps.setString(1, eval.getLikeRestaurant() ? "Y" : "N");
            ps.setDate(2, new java.sql.Date(eval.getVisitDate().getTime()));
            ps.setString(3, eval.getIpAddress());
            ps.setInt(4, eval.getRestaurant().getId());

            ps.executeUpdate();
            try (PreparedStatement ps2 = connection.prepareStatement("SELECT SEQ_EVAL.CURRVAL FROM dual");
                 ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    eval.setId(rs.getInt(1));
                }
            }
            addToCache(eval);
            return eval;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
    }

    @Override
    public boolean update(BasicEvaluation eval) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY )) {
            ps.setString(1, Boolean.TRUE.equals(eval.getLikeRestaurant()) ? "Y" : "N");
            ps.setDate(2, new java.sql.Date(eval.getVisitDate().getTime()));
            ps.setString(3, eval.getIpAddress());
            ps.setInt(4, eval.getRestaurant().getId());
            ps.setInt(5, eval.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
    }

    @Override
    public boolean delete(BasicEvaluation eval) {
        return deleteById(eval.getId());
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
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting like eval", e);
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
}
