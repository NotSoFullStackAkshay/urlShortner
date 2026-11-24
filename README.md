# URL Shortener Microservice

Java 17 · Spring Boot 3.5 · Spring Data JPA · H2 (file-based) · Actuator

## Run

```bash
mvn spring-boot:run          # or: mvn clean package && java -jar target/url-shortener-1.0.0.jar
mvn test                     # run the tests
```

Docker:

```bash
docker build -t url-shortener .
docker run -p 8080:8080 -v urldata:/app/data url-shortener
```

## API

| Method | Path               | Description                          | Success |
|--------|--------------------|--------------------------------------|---------|
| POST   | `/api/urls`        | Create a short URL                   | 201     |
| GET    | `/api/urls/{code}` | Stats (click count, expiry, etc.)    | 200     |
| DELETE | `/api/urls/{code}` | Delete a short URL                   | 204     |
| GET    | `/{code}`          | Redirect to the original URL         | 302     |
| GET    | `/actuator/health` | Health check                         | 200     |

Errors (RFC 7807 JSON): 400 invalid input, 404 unknown code, 409 alias taken, 410 expired.

### Examples

```bash
# create (alias and expiresInDays are optional)
curl -X POST localhost:8080/api/urls -H 'Content-Type: application/json' \
  -d '{"url":"https://www.example.com/a/very/long/path","alias":"docs","expiresInDays":30}'

# follow the redirect
curl -i localhost:8080/docs

# stats
curl localhost:8080/api/urls/docs
```

## Configuration (`application.yml`)

- `app.code-length` - length of generated codes (default 7, base62)
- `spring.datasource.url` - swap H2 for PostgreSQL/MySQL by changing the URL and driver dependency

## Notes

- Expired links are purged hourly by `ExpiredUrlCleaner`.
- No authentication: add Spring Security (or an API gateway) before exposing POST/DELETE publicly.
