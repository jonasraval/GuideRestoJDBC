package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.*;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation>{

    private final Connection connection;
    private RestaurantMapper restaurantMapper;

    private Map<Integer, BasicEvaluation> basicEvaluationCache = new HashMap<>();

    public BasicEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    public void setRestaurantMapper(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    private BasicEvaluation addToCache(ResultSet rs) throws SQLException{
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
        if (basicEvaluationCache.containsKey(id)) {
            return basicEvaluationCache.get(id);
        }

        String sql = "SELECT * FROM LIKES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return addToCache(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
        return null;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        resetCache();

        Set<BasicEvaluation> basicEvaluationSet = new HashSet<>();
        String sql = "SELECT * FROM LIKES";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                basicEvaluationSet.add(addToCache(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
        return basicEvaluationSet;
    }

    public int countLikesForRestaurant(int restaurantId, boolean like) {
        String sql = "SELECT COUNT(*) FROM LIKES WHERE FK_REST = ? AND APPRECIATION = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String insertSql =
                "INSERT INTO LIKES (NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST) " +
                        "VALUES (SEQ_EVAL.NEXTVAL, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {

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

            return eval;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
    }

    @Override
    public boolean update(BasicEvaluation eval) {
        String sql = "UPDATE LIKES SET APPRECIATION=?, DATE_EVAL=?, ADRESSE_IP=?, FK_REST=? WHERE NUMERO=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = "DELETE FROM LIKES WHERE NUMERO=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        return null;
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM LIKES WHERE NUMERO=?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM LIKES";
    }
}
