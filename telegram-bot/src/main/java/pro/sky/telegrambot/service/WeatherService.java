package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.weather.Weather;

public interface WeatherService {

    Weather getWeather (String city);
}
