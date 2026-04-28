# Weather App

Simple Open weather Backend + Frontend app.

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

### Run The Backend

From the `backend` directory:

```bash
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` with the `/api` context path, so the base URL is:

```text
http://localhost:8080/api
```

Swagger UI API specification:

```text
http://localhost:8080/api/swagger-ui.html
```

### Example cURL Commands

Current weather:

```bash
curl "http://localhost:8080/api/weather/current?city=Warsaw&units=metric"
```

Hourly forecast:

```bash
curl "http://localhost:8080/api/weather/forecast/hourly?city=Warsaw&hours=12&units=metric"
```

Daily forecast:

```bash
curl "http://localhost:8080/api/weather/forecast/daily?city=Warsaw&days=7&units=metric"
```

Weather alerts:

```bash
curl "http://localhost:8080/api/weather/alerts?city=Warsaw"
```

Weather summary:

```bash
curl "http://localhost:8080/api/weather/summary?city=Warsaw&units=metric"
```


## Run The Frontend

TODO

When the Angular frontend is created, it will typically be started with:

```bash
ng serve
```