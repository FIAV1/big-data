package com.stormadvance.logprocessing;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;

/**
 * This class contains logic to calculate the country name from IP address
 * 
 */
public class IpToCountryConverter {

	private static DatabaseReader reader = null;

	/**
	 * An parameterised constructor which would take the location of GeoLiteCity.dat
	 * file as input.
	 * 
	 * @param pathToGeoIP2City
	 */
	public IpToCountryConverter(String pathToGeoIP2City) {
		try {
			// A File object pointing to your GeoIP2 or GeoLite2 database
			File database = new File(pathToGeoIP2City);
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
	public String getLocation(String ip) {
		String location;

		try {
			Optional<CityResponse> response = reader.tryCity(InetAddress.getByName(ip));
			if (response.isPresent()) {
				Location l = response.get().getLocation();
				location = "POINT (" + l.getLongitude() + " " + l.getLatitude() + ")";
			} else {
				location = "POINT(0 0)";
			}
		} catch (IOException | GeoIp2Exception e) {
			System.out.println("Error while converting IP to a location");
			location = "POINT(0 0)";
		}

		return location;
	}

	/**
	 * This method takes ip address an input and convert it into country ISO code.
	 * 
	 * @param ip
	 * @return
	 */
	public String getCountryIsoCode(String ip) {
		String countryName;

		try {
			Optional<CityResponse> response = reader.tryCity(InetAddress.getByName(ip));
			if (response.isPresent()) {
				Country country = response.get().getCountry();
				countryName = country.getIsoCode();
			} else {
				countryName = "NA";
			}
		} catch (IOException | GeoIp2Exception e) {
			System.out.println("Error while converting IP to a country code.");
			countryName = "NA";
		}

		return countryName;
	}
}