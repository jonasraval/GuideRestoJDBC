package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class RestaurantService implements IRestaurantService {

    private final RestaurantMapper restaurantMapper;
    private final RestaurantTypeMapper restaurantTypeMapper;
    private final CityMapper cityMapper;
    private final PersistanceContext persistanceContext;

    public RestaurantService(PersistanceContext persistenceContext) {
        this.persistanceContext = persistenceContext;
        this.restaurantMapper = persistenceContext.getRestaurantMapper();
        this.restaurantTypeMapper = persistenceContext.getRestaurantTypeMapper();
        this.cityMapper = persistenceContext.getCityMapper();
    }

    @Override
    public Set<Restaurant> getAllRestaurants() {
        return restaurantMapper.findAll();
    }

    @Override
    public Set<Restaurant> getRestaurantsByName(String research) {
        return restaurantMapper.findByName(research);
    }

    @Override
    public Set<Restaurant> getRestaurantsByCity(String research) {
        return restaurantMapper.findByCity(research);
    }

    @Override
    public Set<RestaurantType> getAllRestaurantsTypes() {
        return restaurantTypeMapper.findAll();
    }

    @Override
    public Set<Restaurant> getRestaurantsByType(RestaurantType restaurantType) {
        return restaurantMapper.findByType(restaurantType.getId());
    }

    @Override
    public Set<City> getAllCities() {
        return cityMapper.findAll();
    }

    @Override
    public City createCity(String ZipCode, String cityName) throws Exception {
        Connection connection = persistanceContext.getConnection();
        try {
            City city = new City(ZipCode, cityName);
            City createdCity = cityMapper.create(city);
            connection.commit();
            return createdCity;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {}
            throw e; //renvoyer la bonne exception, pas celle du rollback...
        }
    }

    @Override
    public Restaurant createRestaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType restaurantType) throws Exception {
        Restaurant restaurant = new Restaurant(id, name, description, website, street, city, restaurantType);
        Restaurant createdRestaurant;
        Connection connection = persistanceContext.getConnection();

        try {
            createdRestaurant = restaurantMapper.create(restaurant);
            connection.commit(); //commiter avant de faire des changements en mémoire

            city.getRestaurants().add(createdRestaurant);
            restaurantType.getRestaurants().add(createdRestaurant);

            return createdRestaurant;

        } catch (Exception e) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            throw e;
        }
    }

    @Override
    public void editRestaurantType(Restaurant restaurant, RestaurantType newType) {
            if (newType != null && newType != restaurant.getType()) {
                restaurant.getType().getRestaurants().remove(restaurant); // Il faut d'abord supprimer notre restaurant puisque le type va peut-être changer
                restaurant.setType(newType);
                newType.getRestaurants().add(restaurant);
            }
    }

    @Override
    public void updateRestaurant(Restaurant restaurant) throws Exception {
        Connection connection = persistanceContext.getConnection();
        try {
            restaurantMapper.update(restaurant);
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {}
            throw e;
        }
    }

    @Override
    public void editRestaurantAddress(Restaurant restaurant, City newCity) {
            if (newCity != null && newCity != restaurant.getAddress().getCity()) {
                restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
                restaurant.getAddress().setCity(newCity);
                newCity.getRestaurants().add(restaurant);
            }
    }

    @Override
    public void deleteRestaurant(Restaurant restaurant) throws Exception {
        Connection connection = persistanceContext.getConnection();
        try {
            restaurantMapper.delete(restaurant);
            connection.commit();

            restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
            restaurant.getType().getRestaurants().remove(restaurant);
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {}
            throw e;
        }
    }

    public RestaurantType getRestaurantTypeByLabel(String label) {
        for (RestaurantType restaurantType : restaurantTypeMapper.findAll()) {
            if (restaurantType.getLabel().equalsIgnoreCase(label)) {
                return restaurantType;
            }
        }
        return null;
    }

    public Restaurant getRestaurantByExactName(String name) {
        for (Restaurant restaurant : restaurantMapper.findByName(name)) {
            if (restaurant.getName().equalsIgnoreCase(name)) {
                return restaurant;
            }
        }
        return null;
    }

}
