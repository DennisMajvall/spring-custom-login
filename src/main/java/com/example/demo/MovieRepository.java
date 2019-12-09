package com.example.demo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    Movie findByTitle(String title);
    List<Movie> findByTitleContaining(String title);
    List<Movie> findByImdbRating(float imdbRating);

}
