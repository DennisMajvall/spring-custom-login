package com.example.demo.rest;

import org.springframework.data.annotation.Id;

public class Movie {
    @Id
    public String id;
    public String title;
    public float imdbRating;

    public Movie() {}
    public Movie(String title, float imdbRating) {
        this.title = title;
        this.imdbRating = imdbRating;
    }

    // getters and setters
}
