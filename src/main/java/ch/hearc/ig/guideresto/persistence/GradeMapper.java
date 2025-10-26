package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GradeMapper extends AbstractMapper {
    private final Connection connection;

    private Map<Integer, Grade> gradeCache = new HashMap<>();

    private EvaluationCriteriaMapper evaluationCriteriaMapper;
    private CompleteEvaluationMapper completeEvaluationMapper;

    public GradeMapper(Connection connection) {
        this.connection = connection;
    }

    public void setEvaluationCriteriaMapper(EvaluationCriteriaMapper evaluationCriteriaMapper) {
        this.evaluationCriteriaMapper = evaluationCriteriaMapper;
    }

    public void setCompleteEvaluationMapper(CompleteEvaluationMapper completeEvaluationMapper) {
        this.completeEvaluationMapper = completeEvaluationMapper;
    }

    private Grade addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");

        Grade grade = new Grade();
        grade.setId(id);
        grade.setGrade(rs.getInt("NOTE"));
        addToCache(grade);

        int evaluationId = rs.getInt("FK_COMM");
        int criteriaId = rs.getInt("FK_CRIT");

        if (completeEvaluationMapper != null) {
            grade.setEvaluation(completeEvaluationMapper.findById(evaluationId));
        } else {
            System.out.println("completeEvaluationMapper est null");
        }

        if (evaluationCriteriaMapper != null) {
            grade.setCriteria(evaluationCriteriaMapper.findById(criteriaId));
        } else {
            System.out.println("evaluationCriteriaMapper est null ");
        }
        return grade;
    }


    @Override
    public IBusinessObject findById(int id) {
        if (this.gradeCache.containsKey(id)) {
            return this.gradeCache.get(id);
        }

        String selectQuery = "SELECT * FROM notes WHERE numero = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)){
                s.setInt(1, id);
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        return addToCache(rs);
                    }
                }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }

        return null;
    }

    public Set<Grade> findByEvaluationId(int evaluationId) {
        Set<Grade> grades = new HashSet<>();
        String query = "SELECT * FROM notes WHERE fk_comm = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int gradeId = rs.getInt("numero");
                    int note = rs.getInt("note");
                    int criteriaId = rs.getInt("fk_crit");

                    EvaluationCriteria criteria = evaluationCriteriaMapper.findById(criteriaId);

                    Grade grade = new Grade(gradeId, note, null, criteria);
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }

        return grades;
    }


    @Override
    public Set findAll() {
        resetCache();
        Set<Grade> grades = new HashSet<>();
        String selectQuery = "SELECT * FROM notes";

        try (PreparedStatement stmt = connection.prepareStatement(selectQuery);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Grade grade = addToCache(rs);
                grades.add(grade);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+e.getMessage());
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
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+e.getMessage());
        }
        return null;
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
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
    }

    @Override
    public boolean delete(IBusinessObject object) {
        if (!(object instanceof Grade grade)) {
            throw new IllegalArgumentException("Object must be an instance of Grade");
        }

        String deleteQuery = "DELETE FROM notes WHERE numero = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.setInt(1, grade.getId());

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                removeFromCache(grade.getId());
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(int id) {
        String deleteQuery = "DELETE FROM notes WHERE numero = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                removeFromCache(id);
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }
        return false;
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
