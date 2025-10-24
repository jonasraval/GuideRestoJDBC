package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.PersistanceContext;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;

import java.util.Set;

public class RestaurantService implements IRestaurantService {

    private RestaurantMapper restaurantMapper;
    private RestaurantTypeMapper restaurantTypeMapper;
    private CityMapper cityMapper;

    public RestaurantService(PersistanceContext persistenceContext) {
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
    public City createCity(String ZipCode, String cityName) {
        City city = new City(ZipCode, cityName);
        return cityMapper.create(city);
    }

    @Override
    public Restaurant createRestaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType restaurantType) {
        Restaurant restaurant = new Restaurant(id, name, description, website, street, city, restaurantType);

        city.getRestaurants().add(restaurant);
        restaurantType.getRestaurants().add(restaurant);

        return restaurantMapper.create(restaurant);
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
    public void updateRestaurant(Restaurant restaurant) {
        restaurantMapper.update(restaurant);
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
    public void deleteRestaurant(Restaurant restaurant) {
        restaurantMapper.delete(restaurant);
        restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        restaurant.getType().getRestaurants().remove(restaurant);
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
