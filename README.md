# Weather App

Simple Open Weather API - Backend + Frontend project.

## Backend Setup

The backend expects an OpenWeather API key through configuration.

Recommended approach:

```bash
export OPENWEATHER_API_KEY=your_api_key_here
```

The backend reads it from `backend/src/main/resources/application.yml` via:

```yaml
weather:
  api-key: ${OPENWEATHER_API_KEY:}
```

You can also place a local override in `backend/src/main/resources/application-local.yml`, but environment variables are preferred.

### Run the Backend

From the `backend` directory:

```bash
cd backend
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`, and weather endpoints are under:

```text
http://localhost:8080/api
```

Swagger UI API specification:

```text
http://localhost:8080/swagger-ui.html
```

### Backend Endpoint Examples

```bash
curl "http://localhost:8080/api/weather/current?city=Warsaw&units=metric"
```

```bash
curl "http://localhost:8080/api/weather/forecast/hourly?city=Warsaw&hours=24&units=metric"
```

```bash
curl "http://localhost:8080/api/weather/forecast/daily?city=Warsaw&days=7&units=metric"
```

```bash
curl "http://localhost:8080/api/weather/alerts?city=Warsaw"
```

```bash
curl "http://localhost:8080/api/weather/summary?city=Warsaw&units=metric"
```

## Run the Frontend

From the `frontend` directory:

```bash
cd frontend
npm install
npx ng serve
```

Or, if Angular CLI is installed globally:

```bash
cd frontend
ng serve
```

The frontend runs on:

```text
http://localhost:4200
```

During development it calls the backend configured in:

```text
frontend/src/environments/environment.ts
```

Default API base URL:

```text
http://localhost:8080/api
```
