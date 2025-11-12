package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.util.Map;
import java.util.Set;

public interface IEvaluationService {
    void addBasicEvaluation(Restaurant restaurant, Boolean like, String ipAddress) throws Exception;
    void evaluateRestaurant(Restaurant restaurant, String username, String comment, Map<EvaluationCriteria, Integer> gradesMap) throws Exception;
    Set<EvaluationCriteria> getAllCriteria();
    int countLikesForRestaurant(int id, boolean like);
}