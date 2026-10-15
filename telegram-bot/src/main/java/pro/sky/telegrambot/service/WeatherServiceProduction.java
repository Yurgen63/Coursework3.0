package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.weather.Weather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("production")
public class WeatherServiceProduction implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceProduction.class);

    private final RestTemplate restTemplate;
    private final String weatherUrl;
    private final String weatherApiKey;

    public WeatherServiceProduction(RestTemplate restTemplate,
                                    @Value("${weather.url}") String weatherUrl,
                                    @Value("${weather.api-key}") String weatherApiKey) {
        this.restTemplate = restTemplate;
        this.weatherUrl = weatherUrl;
        this.weatherApiKey = weatherApiKey;
    }

    @Override
    public Weather getWeather(String city) {
        logger.debug("Requesting weather for city: {}", city);
        ResponseEntity<Weather> response = restTemplate.exchange(
                weatherUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Weather.class,
                city, weatherApiKey
        );
        Weather weather = response.getBody();
        logger.debug("the weather for {} is {}", city, weather);
        return weather;
    }
}
