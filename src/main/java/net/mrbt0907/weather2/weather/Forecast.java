package net.mrbt0907.weather2.weather;

import net.mrbt0907.weather2.weather.storm.WeatherObject;

public class Forecast implements Cloneable {
    public final WeatherObject weather;
    public float accuracy;
    public int stage;

    public Forecast(WeatherObject weather, float accuracy) {
        this.weather = weather;
        this.accuracy = accuracy;
    }

    public Forecast get(float accuracy) {
        Forecast forecast = clone();
        forecast.accuracy = accuracy;
        return forecast;
    }

    public void calculate() {

    }

    @Override
    public Forecast clone() {
        return new Forecast(weather, accuracy);
    }
}