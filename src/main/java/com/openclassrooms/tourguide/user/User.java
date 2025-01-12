package com.openclassrooms.tourguide.user;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.Data;
import lombok.Getter;
import tripPricer.Provider;

@Data
public class User {

	private final UUID userId;
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
	private Date latestLocationTimestamp;

	//rendre les listes thread-safe, ce qui empêche les ConcurrentModificationException.
	@Getter
    private final List<VisitedLocation> visitedLocations = Collections.synchronizedList(new ArrayList<>());
	private final Set<String> attractionsVisited = ConcurrentHashMap.newKeySet();
	private final List<UserReward> userRewards = Collections.synchronizedList(new ArrayList<>());





	private UserPreferences userPreferences = new UserPreferences();
	private List<Provider> tripDeals = new ArrayList<>();




	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	




	
	public void addToVisitedLocations(VisitedLocation visitedLocation) {
		visitedLocations.add(visitedLocation);
	}

    public void clearVisitedLocations() {
		visitedLocations.clear();
	}


	public void addUserReward(UserReward userReward) {
		if (attractionsVisited.add(userReward.attraction.attractionName)) {
			userRewards.add(userReward);
		}
	}



	public VisitedLocation getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}


	public boolean hasRewardForAttraction(Attraction attraction) {
		return attractionsVisited.contains(attraction.attractionName);
	}



}
