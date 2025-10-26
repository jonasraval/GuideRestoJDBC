package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.IBusinessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper {
    private final Connection connection;

    private Map<Integer, EvaluationCriteria> evaluationCriteriaCache = new HashMap<>();

    public EvaluationCriteriaMapper(Connection connection) {
        this.connection = connection;
    }

    private EvaluationCriteria addToCache(ResultSet rs) throws SQLException {
        int id = rs.getInt("NUMERO");

        EvaluationCriteria evaluationCriteria = new EvaluationCriteria();

        evaluationCriteria.setId(id);
        evaluationCriteria.setDescription(rs.getString("DESCRIPTION"));
        evaluationCriteria.setName(rs.getString("NOM"));

        addToCache(evaluationCriteria);

        return evaluationCriteria;
    }

    @Override
    public EvaluationCriteria findById(int id) {
        if (this.evaluationCriteriaCache.containsKey(id)) {
            return this.evaluationCriteriaCache.get(id);
        }

        String selectQuery = "SELECT * FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement s = connection.prepareStatement(selectQuery)) {
            s.setInt(1, id);

            try (ResultSet rs = s.executeQuery()) {
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
    public Set<EvaluationCriteria> findAll() {
        resetCache();
        Set<EvaluationCriteria> criteriaSet = new HashSet<>();
        String selectQuery = "SELECT * FROM criteres_evaluation";

        try (PreparedStatement s = connection.prepareStatement(selectQuery);
             ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                criteriaSet.add(addToCache(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : "+ e.getMessage());
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
                throw new RuntimeException("Erreur d'insertion");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error d'insertion : " + e.getMessage());
        }
    }

    @Override
    public boolean update(IBusinessObject object) {
        if (!(object instanceof EvaluationCriteria criteria)) {
            throw new IllegalArgumentException("Object must be an instance of EvaluationCriteria");
        }

        String updateQuery = "UPDATE criteres_evaluation SET nom = ?, description = ? WHERE numero = ?";

        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setString(1, criteria.getName());
            ps.setString(2, criteria.getDescription());
            ps.setInt(3, criteria.getId());

            int rowsUpdated = ps.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }
    }

    @Override
    public boolean delete(IBusinessObject object) {
        if (!(object instanceof EvaluationCriteria criteria)) {
            throw new IllegalArgumentException("Object must be an instance of EvaluationCriteria");
        }

        String deleteQuery = "DELETE FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.setInt(1, criteria.getId());

            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                removeFromCache(criteria.getId());
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " +e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteById(int id) {
        String deleteQuery = "DELETE FROM criteres_evaluation WHERE numero = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                removeFromCache(id);   // <-- idem ici
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }
        return false;
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
