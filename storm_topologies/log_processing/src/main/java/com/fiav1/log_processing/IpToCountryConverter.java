package com.fiav1.log_processing;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;

/**
 * This class contains logic to calculate the country name from IP address
 * 
 */
public class IpToCountryConverter {

	private static DatabaseReader reader = null;

	public IpToCountryConverter() {
		try {
			// A File object pointing to your GeoIP2 or GeoLite2 database
			File database = new File(getClass().getClassLoader().getResource("GeoLite2-City.mmdb").getFile());
			// This creates the DatabaseReader object. To improve performance, reuse the
			// object across lookups. The object is thread-safe.
			reader = new DatabaseReader.Builder(database).build();
		} catch (Exception exception) {
			throw new RuntimeException("Error occure while initializing IpToCountryConverter class : ");
		}
	}

	/**
	 * This method takes ip address an input and convert it into location.
	 * 
	 * @param ip
	 * @return
	 */
	public Map<String, String> getLocation(String ip) {
		Map<String, String> location = new HashMap<>();

		try {
			Optional<CityResponse> response = reader.tryCity(InetAddress.getByName(ip));
			if (response.isPresent()) {
				location.put("latitude", response.get().getLocation().getLatitude().toString());
				location.put("longitude", response.get().getLocation().getLongitude().toString());
			} else {
				location.put("latitude", "0.0");
				location.put("longitude", "0.0");
			}
		} catch (IOException | GeoIp2Exception e) {
			location.put("latitude", "0.0");
			location.put("longitude", "0.0");
		}

		return location;
	}

	public String getCountryName(String ip) {
		String countryName = "";

		try {
			Optional<CountryResponse> response = reader.tryCountry(InetAddress.getByName(ip));
			if (response.isPresent()) {
				countryName = response.get().getCountry().getIsoCode();
			} else {
				countryName = "N/A";
			}
		} catch (IOException | GeoIp2Exception e) {
			countryName = "N/A";
		}

		return countryName;
	}
}