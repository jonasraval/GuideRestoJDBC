package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.Restaurant;

public interface IRestaurantService {
    void showAllRestaurants();
    void showRestaurantByName(String name);
    void showRestaurantByType(String type);
    void showRestaurantByCity(String city);
    void addRestaurant(Restaurant restaurant);
    void updateRestaurant(Restaurant restaurant);
    void deleteRestaurant(Restaurant restaurant);


}
