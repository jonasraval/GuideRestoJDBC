package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.business.*;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;


public class EvaluationService implements IEvaluationService{
    private final BasicEvaluationMapper basicEvaluationMapper;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final GradeMapper gradeMapper;
    private final PersistanceContext persistanceContext;


    public EvaluationService(PersistanceContext persistanceContext) {
        this.persistanceContext = persistanceContext;
        this.basicEvaluationMapper = persistanceContext.getBasicEvaluationMapper();
        this.evaluationCriteriaMapper = persistanceContext.getEvaluationCriteriaMapper();
        this.completeEvaluationMapper = persistanceContext.getCompleteEvaluationMapper();
        this.gradeMapper = persistanceContext.getGradeMapper();
    }

    @Override
    public void addBasicEvaluation(Restaurant restaurant, Boolean like, String ipAddress) throws Exception {
        Connection connection = persistanceContext.getConnection();
        try {
            BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, like, ipAddress);
            eval = basicEvaluationMapper.create(eval);
            restaurant.getEvaluations().add(eval);
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {}
            throw ex;
        }
    }

    @Override
    public void evaluateRestaurant(Restaurant restaurant, String username, String comment, Map<EvaluationCriteria, Integer> gradesMap) throws Exception{
        Connection connection = persistanceContext.getConnection();
        try {
            CompleteEvaluation evaluation = new CompleteEvaluation(null, new Date(), restaurant, comment, username);
            for (EvaluationCriteria criteria : gradesMap.keySet()) {
                Integer note = gradesMap.get(criteria);
                Grade grade = new Grade(null, note, evaluation, criteria);
                evaluation.getGrades().add(grade);
            }
            completeEvaluationMapper.create(evaluation);
            restaurant.getEvaluations().add(evaluation);
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {}
            throw e;
        }
    }

    public Set<EvaluationCriteria> getAllCriteria() {
        return evaluationCriteriaMapper.findAll();
    }

    @Override
    public int countLikesForRestaurant(int id, boolean like) {
        return basicEvaluationMapper.countLikesForRestaurant(id, like);
    }

}