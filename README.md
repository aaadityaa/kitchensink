# Kitchensink (Migrated Spring Boot + MongoDB)

This is a **modernized version** of the legacy JBoss Kitchensink application, migrated to **Spring Boot (Java 21)** with **MongoDB** as the backend.

---

## Getting Started

### 1. Prerequisites
Make sure you have these installed:
- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker](https://docs.docker.com/get-docker/) (optional, for running MongoDB locally)

---

### 2. Clone the Repository

```bash
git clone https://github.com/aaadityaa/kitchensink.git
cd kitchensink
```

---

### 3. Environment Variables
The application uses the following environment variables.

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | `mongodb://admin:admin@localhost:27017/kitchensink?authSource=admin` |
| `JWT_SECRET` | Secret key for signing JWT tokens | `ChangeThisSecretKey` |
| `JWT_EXPIRATION_MS` | Token expiry time (ms) | `1800000` |

You can set them in your shell before running:

```bash
export SPRING_DATA_MONGODB_URI="mongodb://admin:admin@localhost:27017/kitchensink?authSource=admin"
export JWT_SECRET="ChangeThisSecretKey"
export JWT_EXPIRATION_MS=1800000
```

---

### 4. Run MongoDB

**Option A: Run with Docker**
```bash
docker run -d --name mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin \
  mongo:6
```

**Option B: Use MongoDB Atlas**
Update `SPRING_DATA_MONGODB_URI` with your Atlas connection string.

---

### 5. Build the Project
```bash
mvn clean install
```

---

### 6. Run the Application
```bash
mvn spring-boot:run
```

The app will start at:
[http://localhost:9092](http://localhost:9092)

---

## Endpoints

- **Health Check** → `GET /rest/members/check`
- **Auth**
  - `POST /register-user`
  - `POST /login`
- **Users CRUD** → `GET/POST/PUT/DELETE /rest/members/**`

---

## ✅ Run Tests
```bash
mvn test
```

---

## Deployment
For production:
1. Set all environment variables (`SPRING_DATA_MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION_MS`).
2. Build the jar:
   ```bash
   mvn clean package
   ```
3. Run:
   ```bash
   java -jar target/kitchensink.jar
   ```
