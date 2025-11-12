package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.SQLException;
import java.util.Set;

public interface IRestaurantService {
    Set<Restaurant> getAllRestaurants();
    Set<Restaurant> getRestaurantsByName(String research);
    Set<Restaurant> getRestaurantsByCity(String research);
    Set<RestaurantType> getAllRestaurantsTypes();
    Set<Restaurant> getRestaurantsByType(RestaurantType restaurantType);
    Restaurant createRestaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType restaurantType) throws Exception;
    void updateRestaurant(Restaurant restaurant) throws Exception;
    void deleteRestaurant(Restaurant restaurant) throws Exception;
    void editRestaurantAddress(Restaurant restaurant, City newCity);
    void editRestaurantType(Restaurant restaurant, RestaurantType newType);
    Set<City> getAllCities();
    City createCity(String ZipCode, String cityName) throws Exception;
    RestaurantType getRestaurantTypeByLabel(String label);
    Restaurant getRestaurantByExactName(String name);



}
