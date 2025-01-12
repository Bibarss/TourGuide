package com.openclassrooms.tourguide.dto;


import lombok.Data;

@Data
public class NearbyAttractionDTO {
    private String attractionName;
    private double attractionLatitude;
    private double attractionLongitude;
    private double userLatitude;
    private double userLongitude;
    private double distance;
    private int rewardPoints;

    // Constructeur
    public NearbyAttractionDTO(String attractionName, double attractionLatitude, double attractionLongitude,
                               double userLatitude, double userLongitude, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    // Getters et Setters (générés automatiquement)
    // ...

    // Vous pouvez utiliser Lombok pour réduire le code des getters/setters si vous le souhaitez
}