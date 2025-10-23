package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EvaluationService implements IEvaluationService{
    private final BasicEvaluationMapper basicEvaluationMapper;

    public EvaluationService(PersistanceContext persistanceContext) {
        this.basicEvaluationMapper = persistanceContext.getBasicEvaluationMapper();
        this.evaluationCriteriaMapper = persistanceContext.getEvaluationCriteriaMapper();
        this.completeEvaluationMapper = persistanceContext.getCompleteEvaluationMapper();
        this.gradeMapper = persistanceContext.getGradeMapper();
    }

    @Override
    public void addBasicEvaluation(Restaurant restaurant, Boolean like, String ipAddress) {
        BasicEvaluation eval = new BasicEvaluation(null, new Date(), restaurant, like, ipAddress);
        eval = basicEvaluationMapper.create(eval);
        restaurant.getEvaluations().add(eval);
    }

    @Override
    public void evaluateRestaurant(Restaurant restaurant, String username, String comment, Map<EvaluationCriteria, Integer> gradesMap) {
        CompleteEvaluation evaluation = new CompleteEvaluation(null, new Date(), restaurant, comment, username);

        for (EvaluationCriteria criteria : gradesMap.keySet()) {
            Integer note = gradesMap.get(criteria);
            Grade grade = new Grade(null, note, evaluation, criteria);
            evaluation.getGrades().add(grade);
        }

        completeEvaluationMapper.create(evaluation);
        restaurant.getEvaluations().add(evaluation);

    }

    public Set<EvaluationCriteria> getAllCriteria() {
        return evaluationCriteriaMapper.findAll();
    }

    @Override
    public int countLikesForRestaurant(int id, boolean like) {
        return basicEvaluationMapper.countLikesForRestaurant(id, true);
    }
}
