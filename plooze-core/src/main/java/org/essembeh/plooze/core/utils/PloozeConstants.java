package org.essembeh.plooze.core.utils;

public interface PloozeConstants {

	String[] DEFAULT_FIELDS = { "titre", "sous_titre" };
	String EXTENSION = ".mp4";
	String FIELDS_SEPARATOR = ",";
	String DEFAULT_FILENAME_FORMAT = "${titre} - ${sous_titre}" + EXTENSION;
	String ZIP_URL = "http://webservices.francetelevisions.fr/catchup/flux/flux_main.zip";
	String URL_PREFIX = "http://medias2.francetv.fr/catchup-mobile";
}
