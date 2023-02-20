# Summary

## Comments

// A short comment about the assignment in general
It was a pretty nice test assignment in my opinion. The boilerplate helped a lot and there weren't too many endpoints that I needed to implement, but all of the endpoints had their unique twists to them, which made them pretty complicated for me. Since the quantity of endpoints was not big, it didn't get hard to navigate.

I'd like to mention that in most parts this post helped a lot: https://www.bezkoder.com/spring-boot-file-upload/

// Was the Acceptance Criteria easy to understand?
Some of the expected responses were confusing for the metadata, I was not sure whether I am allowed to change the existing code or not. Also there is a typo, in the assignment.md file files/metas endpoint - it says it is a GET endpoint, but then right after that shows that it should be POST. I assumed that GET is still the correct type.
Other than that, yes, it was all pretty clear.

## Which part of the assignment took the most time and why?

I did not get the download endpoint properly working for a lot of the time. I tried a lot of different approaches and I couldn't figure it out. In the end, I tried using the spring boots own ResponseEntity and that solved my problems. It would have been nice to use the same ResponseEntity everywhere, but I did not want to make the whole project use springs ResponseEntity, because a custom one was provided for me in the 'boilerplate'.
Another thing that took a lot of time for me was handling the metadata. I wanted the response to be in the same way as it was in the AC/assignment description, and I tried several different ways like creating a new object for the metadata, which has all of the different fields, I also struggled with giving a hashmap to postman with the additional meta. In the end using a hashmap and objectmapper helped me solve my issues.

## What You learned

Definitely learned more about Spring Boot and how to do back-end related things in it. The only previous experience I have had with Spring Boot is another test assignment, which is also in my github (spring-boot-library), but thanks to the 'boilerplate' this project had, I feel like this one is written a lot better and thanks to the boilerplate I learned the proper way to do some things related to Spring. For example using ResponseEntity object for endpoint responses.
Although I did use Java to write this project I still learned a bit about Kotlin because I had to understand what some of the boilerplate classes were doing and how to modify them to suit my needs. It is interesting and I'd like to learn more about it, but since the syntax was unfamiliar then I decided to do this project in Java, because otherwise it would have taken much more time.

## TODOs

I'll also add things that I think I should've done better.
Tests! Unit tests, functional tests.
Type validation for the upload, for all of the different meta provided too. I tried using types other than String at some fields for the database Entity, but I ran into problems and I still decided to have most fields as Strings eventually. Since there is no validation, a lot of wrong inputs could be given that would most likely break the application.
Multiple files with the same name cannot be uploaded. It would probably make more sense that it is possible, if the volume of files being uploaded/downloaded is big, since everything has tokens anyway.
I think that my exception handling is probably not the best right now and I should learn more about the proper ways of doing it.
