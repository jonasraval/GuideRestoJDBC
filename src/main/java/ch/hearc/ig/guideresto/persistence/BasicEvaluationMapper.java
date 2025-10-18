package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation>{

    private final Connection connection;
    private RestaurantMapper restaurantMapper;

    public BasicEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public BasicEvaluation findById(int id) {
        String sql = "SELECT * FROM LIKES WHERE NUMERO = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer evalId = rs.getInt("NUMERO");
                    Boolean like = "Y".equalsIgnoreCase(rs.getString("APPRECIATION"));
                    Date visitDate = rs.getDate("DATE_EVAL");
                    String ip = rs.getString("ADRESSE_IP");
                    Restaurant r = restaurantMapper.findById(rs.getInt("FK_REST"));

                    return new BasicEvaluation(evalId, visitDate, r, like, ip);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding like eval " + id, e);
        }
        return null;
    }

    @Override
    public Set<BasicEvaluation> findAll() {
        Set<BasicEvaluation> basicEvaluationSet = new HashSet<>();
        String sql = "SELECT * FROM LIKES";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer id = rs.getInt("NUMERO");
                Boolean like = "Y".equalsIgnoreCase(rs.getString("APPRECIATION"));
                Date visitDate = rs.getDate("DATE_EVAL");
                String ip = rs.getString("ADRESSE_IP");
                Restaurant r = restaurantMapper.findById(rs.getInt("FK_REST"));

                basicEvaluationSet.add(new BasicEvaluation(id, visitDate, r, like, ip));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all likes", e);
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
            throw new RuntimeException("Error counting likes", e);
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

            // Maintenant on récupère le dernier ID utilisé par CE trigger
            try (PreparedStatement ps2 = connection.prepareStatement("SELECT SEQ_EVAL.CURRVAL FROM dual");
                 ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    eval.setId(rs.getInt(1));
                }
            }

            return eval;

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting BasicEvaluation", e);
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
            throw new RuntimeException("Error updating like eval", e);
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
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting like eval", e);
        }
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
