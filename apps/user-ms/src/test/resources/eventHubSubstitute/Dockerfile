# Step 1: Build the project
FROM maven:3.8.4-openjdk-17 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and the src directory to the container
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Step 2: Create the final image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/target/notification-service.jar app.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
