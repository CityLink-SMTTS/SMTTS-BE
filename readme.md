com.smtts
│
├── config                  # Security config, JWT config, CORS, etc.
│   ├── JwtAuthFilter.java
│   ├── SecurityConfig.java
│   └── WebConfig.java
│
├── controller              # REST controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── VehicleController.java
│   └── ReviewController.java
│
├── dto                     # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   ├── ReviewDto.java
│   └── VehicleDto.java
│
├── entity                  # JPA entities / database models
│   ├── User.java
│   ├── Role.java
│   ├── Vehicle.java
│   ├── Trip.java
│   └── Review.java
│
├── exception               # Custom exceptions & handlers
│   ├── ResourceNotFoundException.java
│   ├── FileStorageException.java
│   └── GlobalExceptionHandler.java
│
├── repository              # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── VehicleRepository.java
│   ├── TripRepository.java
│   └── ReviewRepository.java
│
├── security                # JWT token handling, auth utils
│   ├── JwtTokenProvider.java
│   ├── JwtUtils.java
│   └── CustomUserDetailsService.java
│
├── service                 # Business logic / service layer
│   ├── UserService.java
│   ├── AuthService.java
│   ├── VehicleService.java
│   ├── ReviewService.java
│   └── TripService.java
│
├── util                    # Utility classes
│   ├── FileUploadUtil.java
│   ├── AppConstants.java
│   └── GreenScoreCalculator.java
│
└── SmttsApplication.java   # Main Spring Boot application class
