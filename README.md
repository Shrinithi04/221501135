## URL Shortener Service – Spring Boot Project

### Overview

This project is a secure, token-based URL shortening service built using Spring Boot. It includes:

* URL shortening with optional custom codes
* Token-based authentication using client ID and secret
* Automatic link expiry
* Click tracking and basic analytics
* Integration with an external log API for event tracking

---

### Technologies Used

* Java 17+
* Spring Boot
* Spring Web
* SLF4J (Logging)
* REST APIs
* Postman (for testing)
* External HTTP Logging API

---

### Features

* Login and URL shortening functionality
* Token-based authentication
* Integration with an external logging API
* Redirect to original URL using shortcode
* Use of Postman for API testing
* Expected endpoints and responses
* External log API integration for event monitoring

---





### Project Structure

```
└── 221501135
    └── demo
        ├── HELP.md
        ├── mvnw
        ├── mvnw.cmd
        ├── pom.xml
        ├── src
            ├── main
            │   ├── java
            │   │   └── demo
            │   │   │   └── example
            │   │   │       └── demo
            │   │   │           └── DemoApplication.java
            │   └── resources
            │   │   └── application.properties
            └── test
            │   └── java
            │       └── demo
            │           └── example
            │               └── demo
            │                   └── DemoApplicationTests.java
        └── target
            └── classes
                └── application.properties

```

---

### Setup Instructions

1. **Prerequisites**

   * Java 17 or higher installed
   * Maven installed
   * Internet connection for dependency download

2. **Clone the Project**

   ```bash
   git clone https://github.com/your-repo/url-shortener.git
   cd url-shortener
   ```

3. **Build the Project**

   ```bash
   mvn clean install
   ```

4. **Run the Application**

   ```bash
   mvn spring-boot:run
   ```

---

### API Endpoints

#### 1. Login Endpoint

* **POST** `/shorturl/login`
* **Request Body**

  ```json
  {
    "clientID": "user1",
    "clientSecret": "pass123"
  }
  ```
* **Response**

  ```json
  {
    "token_type": "Bearer",
    "access_token": "Bearer eyJhbGciOi...",
    "expires_in": "3600"
  }
  ```

#### 2. Create Short URL

* **POST** `/shorturl`
* **Request Body**

  ```json
  {
    "url": "https://example.com",
    "validity": 60,
    "shortcode": "custom123"
  }
  ```
* **Response**

  ```json
  {
    "shortlink": "http://localhost:8080/shorturl/custom123",
    "expiry": "2025-07-05T10:45:00Z"
  }
  ```

#### 3. Redirect / View Stats

* **GET** `/shorturl/{shortcode}`

* Optional Query Param: `?stats=true`

* **Redirect Response**
  Redirecting to: [https://example.com](https://example.com)

* **Stats Response**

  ```json
  {
    "totalClicks": 1,
    "originalUrl": "https://example.com",
    "creationDate": "...",
    "expiryDate": "...",
    "clickDetails": {
      "timestamp": "...",
      "source": "Unknown",
      "location": "Unknown"
    }
  }
  ```

---

### Logging System

* Logs are sent to an external endpoint:
  `http://20.244.56.144/evaluation-service/logs`
* Bearer Token and Authorization Header used in each log request
* Messages are trimmed to 48 characters (as per API restriction)

---

### Security

* Token-based authentication using simple clientID/clientSecret
* Only authenticated clients receive a valid token for access

---

### How to Add Postman Screenshots

Once the server runs successfully:

1. Open Postman
2. Hit the API endpoints (e.g., login, create short URL)
3. Take screenshots of request and response
4. Add them to the `screenshots/` directory
5. Reference them in the README under a new section:

```
### Postman Test Results

**Login Success**


**Shorten URL**

```

---

### Troubleshooting

* If your app doesn't start:

  * Ensure `@SpringBootApplication` is on the main class
  * Ensure dependencies are in `pom.xml` (Spring Web, Lombok, etc.)
  * Ensure ports are not already used (default is 8080)

* If the Bearer token log API fails:

  * Double-check Authorization header format
  * Limit the log message to 48 characters
  * Make sure you're online and the API is reachable

```

---

### Setup Instructions

1. **Prerequisites**

   * Java 17 or higher installed
   * Maven installed
   * Internet connection for dependency download

2. **Clone the Project**

   ```bash
   git clone https://github.com/your-repo/url-shortener.git
   cd url-shortener
   ```

3. **Build the Project**

   ```bash
   mvn clean install
   ```

4. **Run the Application**

   ```bash
   mvn spring-boot:run
   ```

---

### API Endpoints

#### 1. Login Endpoint

* **POST** `/shorturl/login`
* **Request Body**

  ```json
  {
    "clientID": "user1",
    "clientSecret": "pass123"
  }
  ```
* **Response**

  ```json
  {
    "token_type": "Bearer",
    "access_token": "Bearer eyJhbGciOi...",
    "expires_in": "3600"
  }
  ```

#### 2. Create Short URL

* **POST** `/shorturl`
* **Request Body**

  ```json
  {
    "url": "https://example.com",
    "validity": 60,
    "shortcode": "custom123"
  }
  ```
* **Response**

  ```json
  {
    "shortlink": "http://localhost:8080/shorturl/custom123",
    "expiry": "2025-07-05T10:45:00Z"
  }
  ```

#### 3. Redirect / View Stats

* **GET** `/shorturl/{shortcode}`

* Optional Query Param: `?stats=true`

* **Redirect Response**
  Redirecting to: [https://example.com](https://example.com)

* **Stats Response**

  ```json
  {
    "totalClicks": 1,
    "originalUrl": "https://example.com",
    "creationDate": "...",
    "expiryDate": "...",
    "clickDetails": {
      "timestamp": "...",
      "source": "Unknown",
      "location": "Unknown"
    }
  }
  ```

---

### Logging System

* Logs are sent to an external endpoint:
  `http://20.244.56.144/evaluation-service/logs`
* Bearer Token and Authorization Header used in each log request
* Messages are trimmed to 48 characters (as per API restriction)

---

### Security

* Token-based authentication using simple clientID/clientSecret
* Only authenticated clients receive a valid token for access

---

### How to Add Postman Screenshots

Once the server runs successfully:

1. Open Postman
2. Hit the API endpoints (e.g., login, create short URL)
3. Take screenshots of request and response
4. Add them to the `screenshots/` directory
5. Reference them in the README under a new section:

```
### Postman Test Results

**Login Success**


**Shorten URL**

```

---

### Troubleshooting

* If your app doesn't start:

  * Ensure `@SpringBootApplication` is on the main class
  * Ensure dependencies are in `pom.xml` (Spring Web, Lombok, etc.)
  * Ensure ports are not already used (default is 8080)

* If the Bearer token log API fails:

  * Double-check Authorization header format
  * Limit the log message to 48 characters
  * Make sure you're online and the API is reachable
