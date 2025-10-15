package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluationMapper  extends AbstractMapper{
    private final Connection connection;
    private final RestaurantMapper restaurantMapper;
    private final GradeMapper gradeMapper;

    public CompleteEvaluationMapper(Connection connection) {
        this.connection = connection;
        this.gradeMapper = new GradeMapper(connection);
        this.restaurantMapper = new RestaurantMapper(connection);
    }

    @Override
    public CompleteEvaluation findById(int id) {
        String selectQuery = "SELECT * FROM commentaires WHERE NUMERO = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)) {
            s.setInt(1,id);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    int evaluationId = rs.getInt("numero");
                    Date evaluationDate = rs.getDate("date_eval");
                    String comment = rs.getString("commentaire");
                    String username = rs.getString("nom_utilisateur");
                    int restaurantId = rs.getInt("fk_rest");

                    Restaurant restaurant = restaurantMapper.findById(restaurantId);

                    CompleteEvaluation evaluation = new CompleteEvaluation(
                            evaluationDate,
                            restaurant,
                            comment,
                            username
                    );

                    Set<Grade> grades = gradeMapper.findByEvaluationId(evaluationId);
                    for (Grade grade : grades) { //each grade knows its evaluation
                        grade.setEvaluation(evaluation);
                    }
                    evaluation.setGrades(grades); //the evaluation knows its grades

                    return evaluation;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding evaluation with id " + id, e);
        }
        return null;
    }

    @Override
    public Set<CompleteEvaluation> findAll() {
        Set<CompleteEvaluation> evaluations = new HashSet<>();
        String selectQuery = "SELECT * FROM commentaires";

        try (PreparedStatement s = connection.prepareStatement(selectQuery);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                int evaluationId = rs.getInt("numero");
                Date evaluationDate = rs.getDate("date_eval");
                String comment = rs.getString("commentaire");
                String username = rs.getString("nom_utilisateur");
                int restaurantId = rs.getInt("fk_rest");

                Restaurant restaurant = restaurantMapper.findById(restaurantId);

                CompleteEvaluation evaluation = new CompleteEvaluation(
                        evaluationDate,
                        restaurant,
                        comment,
                        username
                );

                Set<Grade> grades = gradeMapper.findByEvaluationId(evaluationId); //ajouter cette méthode dans GradeMapper!
                evaluation.setGrades(grades);

                evaluations.add(evaluation);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all evaluations", e);
        }
        return evaluations;
    }

    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        Set<CompleteEvaluation> evaluations = new HashSet<>();
        String query = "SELECT * FROM commentaires WHERE fk_rest = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurantId);

            try (ResultSet rs = stmt.executeQuery()) {
                GradeMapper gradeMapper = new GradeMapper(connection);
                RestaurantMapper restaurantMapper = new RestaurantMapper(connection);

                while (rs.next()) {
                    int evaluationId = rs.getInt("numero");
                    Date evaluationDate = rs.getDate("date_eval");
                    String comment = rs.getString("commentaire");
                    String username = rs.getString("nom_utilisateur");
                    Restaurant restaurant = (Restaurant) restaurantMapper.findById(restaurantId);

                    CompleteEvaluation evaluation = new CompleteEvaluation(
                            evaluationId,
                            evaluationDate,
                            restaurant,
                            comment,
                            username
                    );
                    Set<Grade> grades = gradeMapper.findByEvaluationId(evaluationId);

                    for (Grade grade : grades) {
                        grade.setEvaluation(evaluation);
                    }
                    evaluation.setGrades(grades);

                    evaluations.add(evaluation);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding evaluations for restaurant ID " + restaurantId, e);
        }

        return evaluations;
    }



    @Override
    public CompleteEvaluation create(IBusinessObject object) {
        if (!(object instanceof CompleteEvaluation evaluation)) {
            throw new IllegalArgumentException("Object must be an instance of CompleteEvaluation");
        }
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
            } else {
                throw new RuntimeException("Error inserting evaluation");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting evaluation", e);
        }
    }

    @Override
    public boolean update(IBusinessObject object) {
        if (!(object instanceof CompleteEvaluation evaluation)) {
            throw new IllegalArgumentException("Object must be an instance of CompleteEvaluation");
        }

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
            throw new RuntimeException("Error updating evaluation with id " + evaluation.getId(), e);
        }
    }

    @Override
    public boolean delete(IBusinessObject object) {
        if (!(object instanceof CompleteEvaluation evaluation)) {
            throw new IllegalArgumentException("Object must be an instance of CompleteEvaluation");
        }

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

            try (PreparedStatement s = connection.prepareStatement(deleteQuery)) {
                s.setInt(1, evaluation.getId());
                int rowsDeleted = s.executeUpdate();
                return rowsDeleted > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting evaluation with id " + evaluation.getId(), e);
        }
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
