# Social Media Application API

This is a Spring Boot application that implements basic functionality for managing users and posts in a social media platform, with features such as user registration, login, post creation, and more.

## Prerequisites

Before you start, make sure you have the following installed:

- **Java 17** 
- **Gradle**
- **MySQL** (for local development and testing)

## Setup

1. Configure Database (MySQL):
   ```bash
   CREATE DATABASE social_media;
   ```
2. Update the application.properties file:
   ```bash
   spring.datasource.url=jdbc:mysql://localhost:3306/social_media
   spring.datasource.username=root
   spring.datasource.password=password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```
## Running the Application

You can run the application using Gradle.

### Running the application with Gradle:

Run the following command to start the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```
The application will start on http://localhost:8081

### Register a New User
   ```bash
   curl --location 'http://localhost:8081/users/register' \
   --header 'Content-Type: application/json' \
   --data-raw '{
   "username": "Yahya.khan",
   "email": "yahya@example.com",
   "password": "yahya123",
   "profilePicture": "profile.jpg",
   "bio": "Lover of Mechanics!"
   }'
   ```
### Login User
   ```bash
   curl --location 'http://localhost:8081/users/login' \
   --header 'Content-Type: application/json' \
   --data '{
   "username": "Yahya.khan",
   "password":"yahya123"
   }'
   ```
### Get User by ID
   ```bash
   curl --location 'http://localhost:8081/users/1' \
   --header 'Authorization: Bearer <your_token>'
   ```
## Follow a User
```bash
curl --location 'http://localhost:8081/users/1/2/follow' \
--header 'Authorization: Bearer <your_token>' \
--header 'Content-Type: application/json' \
--data ''
```
### Get Followers of a User
```bash
curl --location 'http://localhost:8081/users/2/followers' \
--header 'Authorization: Bearer <your_token>'
```
### Search Users by Keyword
```bash
curl --location --request POST 'http://localhost:8081/users/search?keyword=yahya' \
--header 'Authorization: Bearer <your_token>'
```
### Create a Post
```bash
curl --location 'http://localhost:8081/posts' \
--header 'Authorization: Bearer <your_token>' \
--header 'Content-Type: application/json' \
--data '{
"user": { "userID": 1 },
"content": "My first post!"
}'
```
### Get Posts with Pagination
```bash
curl --location 'http://localhost:8081/posts?page=0&size=20&sort=timestamp%2Cdesc' \
--header 'Authorization: Bearer <your_token>'
```
### Get Post by ID
```bash
curl --location 'http://localhost:8081/posts/1' \
--header 'Authorization: Bearer <your_token>'
```
### Update a Post
```bash
curl --location --request PUT 'http://localhost:8081/posts/1' \
--header 'Authorization: Bearer <your_token>' \
--header 'Content-Type: application/json' \
--data '{
"user": { "userID": 1 },
"content": "My second post!"
}'
```
### Delete a Post
```bash
curl --location --request DELETE 'http://localhost:8081/posts/3' \
--header 'Authorization: Bearer <your_token>'
```
### Post Search
```bash
curl --location --request POST 'http://localhost:8081/posts/search?keyword=se&page=0&size=5' \
--header 'Authorization: Bearer <your_token>'
```
### Add Comment to Post
```bash
curl --location 'http://localhost:8081/posts/1/comments' \
--header 'Authorization: Bearer <your_token>' \
--header 'Content-Type: application/json' \
--data '{
"content": "This is a sample comment",
"user": {
"id": 1
}
}'
```# Social-Media-Application
