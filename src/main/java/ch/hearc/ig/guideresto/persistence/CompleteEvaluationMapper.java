package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompleteEvaluationMapper  extends AbstractMapper<CompleteEvaluation>{
    private final Connection connection;
    private RestaurantMapper restaurantMapper;
    private GradeMapper gradeMapper;
    private EvaluationCriteriaMapper evaluationCriteriaMapper;

    public CompleteEvaluationMapper(Connection connection) {
        this.connection = connection;
    }

    public void setRestaurantMapper(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    public void setGradeMapper(GradeMapper gradeMapper) {
        this.gradeMapper = gradeMapper;
    }

    public void setEvaluationCriteriaMapper(EvaluationCriteriaMapper evaluationCriteriaMapper) {
        this.evaluationCriteriaMapper = evaluationCriteriaMapper;
    }

    private CompleteEvaluation mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");
            CompleteEvaluation completeEvaluation = new CompleteEvaluation();
            completeEvaluation.setId(id);
            completeEvaluation.setComment(rs.getString("COMMENTAIRE"));
            completeEvaluation.setUsername(rs.getString("NOM_UTILISATEUR"));
            completeEvaluation.setVisitDate(rs.getDate("DATE_EVAL"));

            //Restaurant
            int restaurantId = rs.getInt("FK_REST");

            if (restaurantMapper != null) {
                completeEvaluation.setRestaurant(restaurantMapper.findById(restaurantId));
            }

            //Grades
            if (gradeMapper != null) {
                Set<Grade> grades = gradeMapper.findByEvaluationId(id);

                for (Grade grade : grades) {
                    grade.setEvaluation(completeEvaluation);
                }
                completeEvaluation.setGrades(grades);
            }

            addToCache(completeEvaluation);
        return completeEvaluation;
    }

    @Override
    public CompleteEvaluation findById(int id) {
        CompleteEvaluation cacheCompleteEvaluation = getFromCache(id);
        if (!isCacheEmpty()) return cacheCompleteEvaluation;

        String selectQuery = "SELECT * FROM commentaires WHERE NUMERO = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)) {
            s.setInt(1,id);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur " + e.getMessage());
        }
        return null;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        resetCache();

        Set<CompleteEvaluation> evaluations = new HashSet<>();
        String selectQuery = "SELECT * FROM commentaires";

        try (PreparedStatement s = connection.prepareStatement(selectQuery);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                evaluations.add(addToCache(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
        return evaluations;
    }

    public Set<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        Set<CompleteEvaluation> evaluations = new HashSet<>();
        String query = "SELECT * FROM commentaires WHERE fk_rest = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurant.getId());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int evaluationId = rs.getInt("numero");
                    Date evaluationDate = rs.getDate("date_eval");
                    String comment = rs.getString("commentaire");
                    String username = rs.getString("nom_utilisateur");

                    CompleteEvaluation evaluation = new CompleteEvaluation(
                            evaluationId,
                            evaluationDate,
                            restaurant,
                            comment,
                            username
                    );
                    Set<Grade> grades = this.gradeMapper.findByEvaluationId(evaluationId);

                    for (Grade grade : grades) {
                        grade.setEvaluation(evaluation);
                    }
                    evaluation.setGrades(grades);

                    evaluations.add(evaluation);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
        return evaluations;
    }



    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        String insertQuery = "INSERT INTO commentaires (numero, date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement s = connection.prepareStatement(insertQuery)) {
            int nextId = getSequenceValue();
            s.setInt(1, nextId);
            s.setDate(2, new java.sql.Date(evaluation.getVisitDate().getTime()));            s.setString(3, evaluation.getComment());
            s.setString(4, evaluation.getUsername());
            s.setInt(5, evaluation.getRestaurant().getId());

            int rowsInserted = s.executeUpdate();

            if (rowsInserted > 0) {
                evaluation.setId(nextId);
                if (evaluation.getGrades() != null && !evaluation.getGrades().isEmpty()) {
                    GradeMapper gradeMapper = new GradeMapper(connection);
                    for (Grade grade : evaluation.getGrades()) {
                        grade.setEvaluation(evaluation);
                        gradeMapper.create(grade);
                    }
                }
                return evaluation;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(CompleteEvaluation evaluation) {

        String updateQuery = "UPDATE commentaires SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? WHERE numero = ?";

        try (PreparedStatement s = connection.prepareStatement(updateQuery)) {
            s.setDate(1, new java.sql.Date(evaluation.getVisitDate().getTime()));
            s.setString(2, evaluation.getComment());
            s.setString(3, evaluation.getUsername());
            s.setInt(4, evaluation.getRestaurant().getId());
            s.setInt(5, evaluation.getId());

            int rowsUpdated = s.executeUpdate();

            if (rowsUpdated > 0) {
                if (evaluation.getGrades() != null && !evaluation.getGrades().isEmpty()) {
                    GradeMapper gradeMapper = new GradeMapper(connection);
                    for (Grade grade : evaluation.getGrades()) {
                        grade.setEvaluation(evaluation);

                        if (grade.getId() != null && gradeMapper.exists(grade.getId())) {
                            gradeMapper.update(grade);
                        } else {
                            gradeMapper.create(grade);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
        }
    }

    @Override
    public boolean delete(CompleteEvaluation evaluation) {

        String deleteQuery = "DELETE FROM commentaires WHERE numero = ?";

        try {
            GradeMapper gradeMapper = new GradeMapper(connection);
            if (evaluation.getGrades() != null && !evaluation.getGrades().isEmpty()) {
                for (Grade grade : evaluation.getGrades()) {
                    if (grade.getId() != null && gradeMapper.exists(grade.getId())) {
                        gradeMapper.delete(grade);
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
                ps.setInt(1, evaluation.getId());
                int rowsDeleted = ps.executeUpdate();
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    removeFromCache(evaluation.getId());
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(int id) {
        CompleteEvaluation evaluation = findById(id);

        if (evaluation != null) {
            return delete(evaluation);
        } else {
            return false;
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_EVAL.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM commentaires WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM commentaires";
    }


}
