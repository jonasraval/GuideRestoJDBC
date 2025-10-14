package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.IBusinessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper {
    private final Connection connection;

    public EvaluationCriteriaMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public IBusinessObject findById(int id) {
        String selectQuery = "SELECT * FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    int criteriaId = rs.getInt("numero");
                    String name = rs.getString("nom");
                    String description = rs.getString("description");

                    return new EvaluationCriteria(criteriaId, name, description);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding evaluation criteria with id " + id, e);
        }
        return null;
    }

    @Override
    public Set findAll() {
        Set<IBusinessObject> criteriaSet = new HashSet<>();
        String selectQuery = "SELECT * FROM criteres_evaluation";

        try (PreparedStatement s = connection.prepareStatement(selectQuery);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("numero");
                String name = rs.getString("nom");
                String description = rs.getString("description");

                EvaluationCriteria criteria = new EvaluationCriteria(id, name, description);
                criteriaSet.add(criteria);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all evaluation criteria", e);
        }

        return criteriaSet;
    }

    @Override
    public IBusinessObject create(IBusinessObject object) {
        if (!(object instanceof EvaluationCriteria criteria)) {
            throw new IllegalArgumentException("Object must be an instance of EvaluationCriteria");
        }

        String insertQuery = "INSERT INTO criteres_evaluation (numero, nom, description) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            int nextId = getSequenceValue();

            stmt.setInt(1, nextId);
            stmt.setString(2, criteria.getName());
            stmt.setString(3, criteria.getDescription());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                return new EvaluationCriteria(nextId, criteria.getName(), criteria.getDescription());
            } else {
                throw new RuntimeException("Error inserting EvaluationCriteria");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting EvaluationCriteria", e);
        }
    }

    @Override
    public boolean update(IBusinessObject object) {
        if (!(object instanceof EvaluationCriteria criteria)) {
            throw new IllegalArgumentException("Object must be an instance of EvaluationCriteria");
        }

        String updateQuery = "UPDATE criteres_evaluation SET nom = ?, description = ? WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, criteria.getName());
            stmt.setString(2, criteria.getDescription());
            stmt.setInt(3, criteria.getId());

            int rowsUpdated = stmt.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating EvaluationCriteria with id " + criteria.getId(), e);
        }
    }

    @Override
    public boolean delete(IBusinessObject object) {
        if (!(object instanceof EvaluationCriteria criteria)) {
            throw new IllegalArgumentException("Object must be an instance of EvaluationCriteria");
        }

        String deleteQuery = "DELETE FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, criteria.getId());

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting EvaluationCriteria with id " + criteria.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String deleteQuery = "DELETE FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting EvaluationCriteria with id " + id, e);
        }
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_CRITERES_EVALUATION.NEXTVAL FROM dual";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM criteres_evaluation WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM criteres_evaluation";
    }
}
