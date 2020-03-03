# Logs es index creator script
curl -X PUT "localhost:9200/logs?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings" : {
    "properties" : {
      "request": { "type": "keyword" },
      "bytes": { "type": "integer" },
      "auth": { "type": "text" },
      "ident": { "type": "text" },
      "clientip": { "type": "text" },
      "referrer": { "type": "text" },
      "httpversion": { "type": "float" },
      "host": { "type": "text" },
      "verb": { "type": "text" },
      "response": { "type": "integer" },
      "agent": { "type": "text" },
      "country_name" : { "type": "keyword" },
      "coord": { "type": "geo_point" }
    }
  }
}
'

# Twitter es index creator script
curl -X POST "localhost:9200/twitter/sentiment?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings" : {
    "properties" : {
      "id": { "type": "long" },
      "user_name": { "type": "text" },
      "user_screen_name": { "type": "text" },
      "user_location": { "type": "text" },
      "tweet_full_text": { "type": "text" },
      "tweet_created_at": { "type": "date" }
    }
  }
}
'

# Meteo Nowcast es index creator script
curl -X PUT "localhost:9200/meteo_nowcast?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings" : {
    "properties" : {
      "city_id": { "type": "long" },
      "city_name": { "type": "keyword" },
      "temp": { "type": "float" },
      "temp_feels_like": { "type": "float" },
      "temp_min": { "type": "float" },
      "temp_max": { "type": "float" },
      "pressure": { "type": "float" },
      "pressure_sea_level": { "type": "float" },
      "pressure_grnd_level": { "type": "float" },
      "humidity": { "type": "float" },
      "wind_speed": { "type": "float" },
      "wind_deg": { "type": "float" },
      "cloudiness": { "type": "float" },
      "rain_1h" : { "type": "float" },
      "rain_3h" : { "type": "float" },
      "snow_1h" : { "type": "float" },
      "snow_3h" : { "type": "float" },
      "description": { "type": "text" },
      "coord": { "type": "geo_point" }
    }
  }
}
'

# Meteo Forecast es index creator script
curl -X PUT "localhost:9200/meteo_forecast?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings" : {
    "properties" : {
      "city_id": { "type": "long" },
      "city_name": { "type": "keyword" },
      "temp": { "type": "float" },
      "temp_feels_like": { "type": "float" },
      "temp_min": { "type": "float" },
      "temp_max": { "type": "float" },
      "pressure": { "type": "float" },
      "pressure_sea_level": { "type": "float" },
      "pressure_grnd_level": { "type": "float" },
      "humidity": { "type": "float" },
      "wind_speed": { "type": "float" },
      "wind_deg": { "type": "float" },
      "cloudiness": { "type": "float" },
      "rain_3h" : { "type": "float" },
      "snow_3h" : { "type": "float" },
      "description": { "type": "text" },
      "coord": { "type": "geo_point" }
    }
  }
}
'

# UVI Forecast es index creator script
curl -X PUT "localhost:9200/uvi_forecast?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings" : {
    "properties" : {
      "city_id": { "type": "long" },
      "city_name": { "type": "keyword" },
      "coord": { "type": "geo_point" },
      "uvi": { "type": "float" }
    }
  }
}
'
