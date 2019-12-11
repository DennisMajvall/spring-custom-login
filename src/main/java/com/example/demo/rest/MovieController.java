package com.example.demo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    public static UserDetails currentUserDetails(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            return principal instanceof UserDetails ? (UserDetails) principal : null;
        }
        return null;
    }

    @GetMapping
    Iterable<Movie> getMovies(){
        System.out.println("User details in movies/ " + currentUserDetails());
        return movieRepository.findAll();
    }

    @GetMapping("/{id}")
    Movie getPost(@PathVariable String id){
        return new Movie("hej", 4.0f);
    }

    @PostMapping
    void addPost(@RequestBody Movie body) {
        movieRepository.save(body);
        System.out.println("Added " + body.title + " to DB");
//        repo.save(body);
    }
}