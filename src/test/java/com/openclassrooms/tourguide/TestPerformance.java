package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class TestPerformance {

	/*
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 */

	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		List<CompletableFuture<VisitedLocation>> futures = allUsers.stream()
				.map(user -> tourGuideService.trackUserLocation(user))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + elapsedSeconds + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= elapsedSeconds);
	}

	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

		Attraction attraction = gpsUtil.getAttractions().get(0);

		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		List<CompletableFuture<Void>> rewardFutures = allUsers.stream()
				.map(user -> rewardsService.calculateRewardsAsync(user))
				.collect(Collectors.toList());

		CompletableFuture.allOf(rewardFutures.toArray(new CompletableFuture[0])).join();

		stopWatch.stop();

		long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());
		System.out.println("highVolumeGetRewards: Time Elapsed: " + elapsedSeconds + " seconds.");

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= elapsedSeconds);
	}
}
