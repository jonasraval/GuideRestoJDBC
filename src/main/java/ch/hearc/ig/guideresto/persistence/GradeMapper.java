package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.IBusinessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GradeMapper extends AbstractMapper {
    private final Connection connection;

    public GradeMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public IBusinessObject findById(int id) {
        String selectQuery = "SELECT * FROM grades WHERE numero = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)){
                s.setInt(1, id);
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        int gradeId = rs.getInt("numero");
                        int note = rs.getInt("note");
                        int evaluationId = rs.getInt("fk_comm");
                        int criteriaId = rs.getInt("fk_crit");

                        CompleteEvaluationMapper evaluationMapper = new CompleteEvaluationMapper(connection);
                        EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper(connection);

                        CompleteEvaluation evaluation = evaluationMapper.findById(evaluationId);
                        EvaluationCriteria criteria = criteriaMapper.findById(criteriaId);

                        return new Grade(gradeId, note, evaluation, criteria);
                    }
                }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding grade with id " + id, e);
        }

        return null;
    }

    public Set<Grade> findByEvaluationId(int evaluationId) {
        Set<Grade> grades = new HashSet<>();
        String query = "SELECT * FROM notes WHERE fk_comm = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper(connection);

                while (rs.next()) {
                    int gradeId = rs.getInt("numero");
                    int note = rs.getInt("note");
                    int criteriaId = rs.getInt("fk_crit");

                    EvaluationCriteria criteria = criteriaMapper.findById(criteriaId);

                    Grade grade = new Grade(gradeId, note, null, criteria);
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding grades for evaluation ID " + evaluationId, e);
        }

        return grades;
    }


    @Override
    public Set findAll() {
        Set<Grade> grades = new HashSet<>();
        String selectQuery = "SELECT * FROM notes";

        try (PreparedStatement stmt = connection.prepareStatement(selectQuery);
             ResultSet rs = stmt.executeQuery()) {

            CompleteEvaluationMapper evaluationMapper = new CompleteEvaluationMapper(connection);
            EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper(connection);

            while (rs.next()) {
                int gradeId = rs.getInt("numero");
                int note = rs.getInt("note");
                int evaluationId = rs.getInt("fk_comm");
                int criteriaId = rs.getInt("fk_crit");

                CompleteEvaluation evaluation = evaluationMapper.findById(evaluationId);
                EvaluationCriteria criteria = criteriaMapper.findById(criteriaId);

                Grade grade = new Grade(gradeId, note, evaluation, criteria);
                grades.add(grade);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all grades", e);
        }

        return grades;
    }

    @Override
    public IBusinessObject create(IBusinessObject object) {
        if (!(object instanceof Grade grade)) {
            throw new IllegalArgumentException("Object must be an instance of Grade");
        }

        String insertQuery = "INSERT INTO notes (numero, note, fk_comm, fk_crit) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            int nextId = getSequenceValue();
            stmt.setInt(1, nextId);
            stmt.setInt(2, grade.getGrade());
            stmt.setInt(3, grade.getEvaluation().getId());
            stmt.setInt(4, grade.getCriteria().getId());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                return new Grade(nextId, grade.getGrade(), grade.getEvaluation(), grade.getCriteria());
            } else {
                throw new RuntimeException("Failed to insert grade into database");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating grade", e);
        }
    }

    @Override
    public boolean update(IBusinessObject object) {
        if (!(object instanceof Grade grade)) {
            throw new IllegalArgumentException("Object must be an instance of Grade");
        }

        String updateQuery = "UPDATE notes SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, grade.getGrade());
            stmt.setInt(2, grade.getEvaluation().getId());
            stmt.setInt(3, grade.getCriteria().getId());
            stmt.setInt(4, grade.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating grade with id " + grade.getId(), e);
        }
    }

    @Override
    public boolean delete(IBusinessObject object) {
        if (!(object instanceof Grade grade)) {
            throw new IllegalArgumentException("Object must be an instance of Grade");
        }

        String deleteQuery = "DELETE FROM notes WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, grade.getId());

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting grade with id " + grade.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String deleteQuery = "DELETE FROM notes WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, id);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting grade with id " + id, e);
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_NOTES.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM notes WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM notes";
    }
}
