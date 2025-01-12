package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.Data;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Data
@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private final int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;

	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final AttractionGrid attractionGrid;

	// Cache pour stocker d'éventuelles distances (optionnel)
	private Map<String, Double> distanceCache = new HashMap<>();

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		// Construire une grille lors de l’initialisation
		this.attractionGrid = new AttractionGrid(gpsUtil.getAttractions());
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Calcule les récompenses pour un utilisateur donné en lançant des tâches asynchrones.
	 */
	public void calculateRewards(User user) {
		for (VisitedLocation visitedLocation : user.getVisitedLocations()) {
			// Récupérer seulement les attractions proches de la cellule
			List<Attraction> cellAttractions = attractionGrid.getNearbyAttractions(visitedLocation.location);

			for (Attraction attraction : cellAttractions) {
				// Puis vérifier la distance réelle, etc.
				if (!user.hasRewardForAttraction(attraction)) {
					if (nearAttraction(visitedLocation, attraction)) {
						int points = getRewardPoints(attraction, user);
						user.addUserReward(new UserReward(visitedLocation, attraction, points));
					}
				}
			}
		}
	}

	/**
	 * Vérifie si l'attraction se trouve dans un rayon "attractionProximityRange" (ici 200) du "location".
	 * (Utilisé dans d'autres parties du code si besoin.)
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return !(getDistance(attraction, location) > 200);
	}

	/**
	 * Vérifie si l'attraction se trouve à portée (proximityBuffer) de la position visitée par l'utilisateur.
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	/**
	 * Récupère les points de récompense pour une attraction et un utilisateur donnés.
	 */
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	/**
	 * Calcule la distance (en miles) entre deux positions géographiques (latitude/longitude).
	 */
	public double getDistance(Location loc1, Location loc2) {
		// Ex. : utilisation d'un cache si vous le souhaitez
		String key = loc1.latitude + "," + loc1.longitude + "-" + loc2.latitude + "," + loc2.longitude;
		// (Possibilité de faire "distanceCache.computeIfAbsent(key, k -> calculateDistance(loc1, loc2));")

		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}
}
