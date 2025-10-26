package ch.hearc.ig.guideresto.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public class PersistanceContext implements AutoCloseable {
    private final Connection connection;

    private final BasicEvaluationMapper basicEvaluationMapper;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final GradeMapper gradeMapper;

    private final RestaurantMapper restaurantMapper;
    private final RestaurantTypeMapper restaurantTypeMapper;
    private final CityMapper cityMapper;

    public PersistanceContext() {
        this.connection = ConnectionUtils.getConnection();

        this.basicEvaluationMapper = new BasicEvaluationMapper(connection);
        this.evaluationCriteriaMapper = new EvaluationCriteriaMapper(connection);
        this.completeEvaluationMapper = new CompleteEvaluationMapper(connection);
        this.gradeMapper = new GradeMapper(connection);

        this.restaurantMapper = new RestaurantMapper(connection);
        this.restaurantTypeMapper = new RestaurantTypeMapper(connection);
        this.cityMapper = new CityMapper(connection);

        gradeMapper.setEvaluationCriteriaMapper(evaluationCriteriaMapper);
        completeEvaluationMapper.setGradeMapper(gradeMapper);

        restaurantMapper.setCityMapper(cityMapper);
        restaurantMapper.setRestaurantTypeMapper(restaurantTypeMapper);
        restaurantMapper.setCompleteEvaluationMapper(completeEvaluationMapper);

        completeEvaluationMapper.setRestaurantMapper(restaurantMapper);

    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    public BasicEvaluationMapper getBasicEvaluationMapper() {
        return basicEvaluationMapper;
    }

    public EvaluationCriteriaMapper getEvaluationCriteriaMapper() {
        return evaluationCriteriaMapper;
    }

    public CompleteEvaluationMapper getCompleteEvaluationMapper() {
        return completeEvaluationMapper;
    }

    public GradeMapper getGradeMapper() {
        return gradeMapper;
    }
    public RestaurantMapper getRestaurantMapper() {
        return restaurantMapper;
    }

    public RestaurantTypeMapper getRestaurantTypeMapper() {
        return restaurantTypeMapper;
    }

    public CityMapper getCityMapper() {
        return cityMapper;
    }
}
