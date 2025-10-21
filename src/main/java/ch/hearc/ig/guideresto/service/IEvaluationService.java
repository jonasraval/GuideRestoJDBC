package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.Restaurant;

public interface IEvaluationService {
    void addBasicEvaluation(Restaurant restaurant, Boolean like);
    void evaluateRestaurant(Restaurant restaurant);

}
