package com.openclassrooms.tourguide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;

import java.util.*;

/**
 * Exemple simplifié de grille spatiale pour limiter le nombre d'attractions
 * à vérifier (indexation par cellule).
 */
public class AttractionGrid {
    // Taille d’une cellule de la grille (en degrés de latitude/longitude)
    private static final double CELL_SIZE = 1.0;

    // Map : "cellKey" -> Liste d'attractions
    private final Map<String, List<Attraction>> grid = new HashMap<>();

    /**
     * Construit la grille à partir de la liste complète d'attractions.
     *
     * @param allAttractions liste de toutes les attractions (gpsUtil.getAttractions())
     */
    public AttractionGrid(List<Attraction> allAttractions) {
        for (Attraction attraction : allAttractions) {
            String cellKey = getCellKey(attraction.latitude, attraction.longitude);
            grid.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(attraction);
        }
    }

    /**
     * Retourne la liste d'attractions de la cellule correspondant à (lat, lon).
     * Note : pour une meilleure précision, on peut également envisager
     * de récupérer les cellules voisines (latIndex±1, lonIndex±1).
     *
     * @param location la position (latitude, longitude) de l'utilisateur
     * @return la liste d'attractions situées dans la même cellule (1 degré x 1 degré)
     */
    public List<Attraction> getNearbyAttractions(Location location) {
        String cellKey = getCellKey(location.latitude, location.longitude);
        return grid.getOrDefault(cellKey, Collections.emptyList());
    }

    /**
     * Calcule l'identifiant de cellule (ex. "2_-3") en fonction de la latitude/longitude.
     */
    private String getCellKey(double lat, double lon) {
        int latCell = (int) (lat / CELL_SIZE);
        int lonCell = (int) (lon / CELL_SIZE);
        return latCell + "_" + lonCell;
    }
}
