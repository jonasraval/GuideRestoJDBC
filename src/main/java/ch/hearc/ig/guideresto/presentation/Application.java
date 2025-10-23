package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.service.EvaluationService;
import ch.hearc.ig.guideresto.service.PersistanceContext;
import ch.hearc.ig.guideresto.service.RestaurantService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);

    private static RestaurantService restaurantService;
    private static EvaluationService evaluationService;


    public Application(RestaurantService restaurantService, EvaluationService evaluationService) {
        this.restaurantService = restaurantService;
        this.evaluationService = evaluationService;
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        try (PersistanceContext persistanceContext = new PersistanceContext()) {

            restaurantService = new RestaurantService(persistanceContext);
            evaluationService = new EvaluationService(persistanceContext);

            scanner = new Scanner(System.in);

            System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
            int choice;
            do {
                printMainMenu();
                choice = readInt();
                proceedMainMenu(choice);
            } while (choice != 0);
        } catch (Exception e) {
            System.err.println("Erreur au démarrage de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affichage du menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice) {
        switch (choice) {
            case 1:
                showRestaurantsList();
                break;
            case 2:
                searchRestaurantsByName();
                break;
            case 3:
                searchRestaurantsByCity();
                break;
            case 4:
                searchRestaurantsByType();
                break;
            case 5:
                addNewRestaurant();
                break;
            case 0:
                System.out.println("Au revoir !");
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * On affiche à l'utilisateur une liste de restaurants numérotés, et il doit en sélectionner un !
     *
     * @param restaurants Liste à afficher
     * @return L'instance du restaurant choisi par l'utilisateur
     */
    private static Restaurant pickRestaurant(Set<Restaurant> restaurants) {
        if (restaurants.isEmpty()) {
            System.out.println("Aucun restaurant n'a été trouvé !");
            return null;
        }

        String result;
        for (Restaurant currentRest : restaurants) {
            result = "";
            result = "\"" + result + currentRest.getName() + "\" - " + currentRest.getAddress().getStreet() + " - ";
            result = result + currentRest.getAddress().getCity().getZipCode() + " " + currentRest.getAddress().getCity().getCityName();
            System.out.println(result);
        }

        System.out.println("Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
        String choice = readString();

        return restaurantService.getRestaurantByExactName(choice);
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");
        Set<Restaurant> restaurants = restaurantService.getAllRestaurants();
        Restaurant restaurant = pickRestaurant(restaurants);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }


    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantsByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();

        Set<Restaurant> filteredList = restaurantService.getRestaurantsByName(research);

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom de la ville contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantsByCity() {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();
        Set<Restaurant> filteredList = restaurantService.getRestaurantsByCity(research);
        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * L'utilisateur choisit une ville parmi celles présentes dans le système.
     *
     * @param cities La liste des villes à présnter à l'utilisateur
     * @return La ville sélectionnée, ou null si aucune ville n'a été choisie.
     */
    private static City pickCity(Set<City> cities) {
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();

        if (choice.equals("NEW")) {
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            String zipCode = readString();
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            String cityName = readString();

            City city = restaurantService.createCity(zipCode, cityName);
            System.out.println("Nouvelle ville ajoutée avec succès !");
            return city;
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * L'utilisateur choisit un type de restaurant parmis ceux présents dans le système.
     *
     * @param types La liste des types de restaurant à présnter à l'utilisateur
     * @return Le type sélectionné, ou null si aucun type n'a été choisi.
     */
    private static RestaurantType pickRestaurantType(Set<RestaurantType> types) {
        System.out.println("Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
        for (RestaurantType currentType : types) {
            System.out.println("\"" + currentType.getLabel() + "\" : " + currentType.getDescription());
        }
        String choice = readString();
        return restaurantService.getRestaurantTypeByLabel(choice);
    }

    /**
     * L'utilisateur commence par sélectionner un type de restaurant, puis sélectionne un des restaurants proposés s'il y en a.
     * Si l'utilisateur sélectionne un restaurant, ce dernier lui sera affiché.
     */
    private static void searchRestaurantsByType() {
        RestaurantType chosenType = pickRestaurantType(restaurantService.getAllRestaurantsTypes());
        Set<Restaurant> filteredList = restaurantService.getRestaurantsByType(chosenType);
        if (chosenType == null) {
            System.out.println("Aucun type sélectionné. Retour au menu principal.");
            return;
        }

        if (filteredList.isEmpty() || filteredList == null) {
            System.out.println("Aucun restaurant trouvé pour le type choisi.");
            return;
        }

        Restaurant restaurant = pickRestaurant(filteredList);

        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Le programme demande les informations nécessaires à l'utilisateur puis crée un nouveau restaurant dans le système.
     */
    private static void addNewRestaurant() {
        //Affichage
        System.out.println("Vous allez ajouter un nouveau restaurant !");
        System.out.println("Quel est son nom ?");
        String name = readString();
        System.out.println("Veuillez entrer une courte description : ");
        String description = readString();
        System.out.println("Veuillez entrer l'adresse de son site internet : ");
        String website = readString();
        System.out.println("Rue : ");
        String street = readString();
        City city = null;
        do
        {
            city = pickCity(restaurantService.getAllCities());

        } while (city == null);
        RestaurantType restaurantType = null;
        do
        {
            restaurantType = pickRestaurantType(restaurantService.getAllRestaurantsTypes());
        } while (restaurantType == null);

        Restaurant restaurant = restaurantService.createRestaurant(null, name, description, website, street, city, restaurantType);

        showRestaurant(restaurant);
    }

    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis affiche le menu des actions disponibles sur ledit restaurant
     *
     * @param restaurant Le restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        System.out.println("Affichage d'un restaurant : ");
        StringBuilder sb = new StringBuilder();
        sb.append(restaurant.getName()).append("\n");
        sb.append(restaurant.getDescription()).append("\n");
        sb.append(restaurant.getType().getLabel()).append("\n");
        sb.append(restaurant.getWebsite()).append("\n");
        sb.append(restaurant.getAddress().getStreet()).append(", ");
        sb.append(restaurant.getAddress().getCity().getZipCode()).append(" ").append(restaurant.getAddress().getCity().getCityName()).append("\n");

        Integer id = restaurant.getId();
        if (id != null) {
            sb.append("Nombre de likes : ")
                    .append(evaluationService.countLikesForRestaurant(id, true))
                    .append("\n");
            sb.append("Nombre de dislikes : ")
                    .append(evaluationService.countLikesForRestaurant(id, false))
                    .append("\n");
        } else {
            sb.append("Nombre de likes : N/A (non encore enregistré)").append("\n");
            sb.append("Nombre de dislikes : N/A").append("\n");
        }
        sb.append("\nEvaluations reçues : ").append("\n");

        String text;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            text = getCompleteEvaluationDescription(currentEval);
            if (text != null) {
                sb.append(text).append("\n");
            }
        }

        System.out.println(sb);

        int choice;
        do {
            showRestaurantMenu();
            choice = readInt();
            proceedRestaurantMenu(choice, restaurant);
        } while (choice != 0 && choice != 6);
    }

    /**
     * Retourne un String qui contient le détail complet d'une évaluation si elle est de type "CompleteEvaluation". Retourne null s'il s'agit d'une BasicEvaluation
     *
     * @param --eval L'évaluation à afficher
     * @return Un String qui contient le détail complet d'une CompleteEvaluation, ou null s'il s'agit d'une BasicEvaluation
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Editer ce restaurant");
        System.out.println("5. Editer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     *
     * @param choice     Un numéro d'action, entre 0 et 6. Si le numéro ne se trouve pas dans cette plage, l'application ne fait rien et va réafficher le menu complet.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        switch (choice) {
            case 1:
                addBasicEvaluation(restaurant, true);
                break;
            case 2:
                addBasicEvaluation(restaurant, false);
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);
                break;
            case 5:
                editRestaurantAddress(restaurant);
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    /**
     * Ajoute au restaurant passé en paramètre un like ou un dislike, en fonction du second paramètre.
     * L'IP locale de l'utilisateur est enregistrée. S'il s'agissait d'une application web, il serait préférable de récupérer l'adresse IP publique de l'utilisateur.
     *
     * @param restaurant Le restaurant qui est évalué
     * @param like       Est-ce un like ou un dislike ?
     */
    private static void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }

        evaluationService.addBasicEvaluation(restaurant, like, ipAddress);

        System.out.println("Votre vote a été pris en compte !");
    }

    /**
     * Crée une évaluation complète pour le restaurant. L'utilisateur doit saisir toutes les informations (dont un commentaire et quelques notes)
     *
     * @param restaurant Le restaurant à évaluer
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        Map<EvaluationCriteria, Integer> gradesMap = new HashMap<>();

        for (EvaluationCriteria criteria : evaluationService.getAllCriteria()) {
            System.out.println(criteria.getName() + " : " + criteria.getDescription());
            Integer note = readInt();
            gradesMap.put(criteria, note);
        }

        evaluationService.evaluateRestaurant(restaurant, username, comment, gradesMap);

        System.out.println("Votre évaluation a bien été enregistrée, merci !");
    }

    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant (sauf la clé primaire) pour le mettre à jour.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant à modifier
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        System.out.println("Nouveau nom : ");
        restaurant.setName(readString());
        System.out.println("Nouvelle description : ");
        restaurant.setDescription(readString());
        System.out.println("Nouveau site web : ");
        restaurant.setWebsite(readString());
        System.out.println("Nouveau type de restaurant : ");

        RestaurantType newType = pickRestaurantType(restaurantService.getAllRestaurantsTypes());
        restaurantService.editRestaurantType(restaurant, newType);

        restaurantService.updateRestaurant(restaurant);

        System.out.println("Merci, le restaurant a bien été modifié !");
    }

    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant dont l'adresse doit être mise à jour.
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        restaurant.getAddress().setStreet(readString());

        City newCity = pickCity(restaurantService.getAllCities());
        if (newCity != null && newCity != restaurant.getAddress().getCity()) {
            restaurantService.editRestaurantAddress(restaurant, newCity);
        }
        restaurantService.updateRestaurant(restaurant);

        System.out.println("L'adresse a bien été modifiée ! Merci !");
    }

    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et toutes ses évaluations du référentiel.
     *
     * @param restaurant Le restaurant à supprimer.
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();
        if (choice.equals("o") || choice.equals("O")) {

            restaurantService.deleteRestaurant(restaurant);
            System.out.println("Le restaurant a bien été supprimé !");
        }
    }

    /**
     * Recherche dans le Set la ville comportant le code NPA passé en paramètre.
     * Retourne null si la ville n'est pas trouvée
     *
     * @param cities  Set de villes
     * @param zipCode NPA de la ville à rechercher
     * @return L'instance de la ville ou null si pas trouvé
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * readInt ne repositionne pas le scanner au début d'une ligne donc il faut le faire manuellement sinon
     * des problèmes apparaissent quand on demande à l'utilisateur de saisir une chaîne de caractères.
     *
     * @return Un nombre entier saisi par l'utilisateur au clavier
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do {
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Méthode readString pour rester consistant avec readInt !
     *
     * @return Une chaîne de caractères saisie par l'utilisateur au clavier
     */
    private static String readString() {
        return scanner.nextLine();
    }

}
