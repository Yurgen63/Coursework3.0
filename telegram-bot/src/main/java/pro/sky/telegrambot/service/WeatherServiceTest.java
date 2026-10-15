package pro.sky.telegrambot.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.weather.Weather;
import pro.sky.telegrambot.model.weather.WeatherMain;
import pro.sky.telegrambot.model.weather.WeatherWind;

import java.math.BigDecimal;

@Service
@Profile("!production")
public class WeatherServiceTest implements WeatherService {

    @Override
    public Weather getWeather(String city) {
        Weather weather = new Weather();

        WeatherMain main = new WeatherMain();
        main.setTemp(BigDecimal.valueOf(25));
        main.setHumidity(BigDecimal.valueOf(60));

        WeatherWind wind = new WeatherWind();
        wind.setSpeed(BigDecimal.valueOf(5));
        wind.setDeg(180);

        weather.setMain(main);
        weather.setWind(wind);
        return weather;
    }
}

