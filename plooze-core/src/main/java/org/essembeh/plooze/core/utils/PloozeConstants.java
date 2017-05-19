package org.essembeh.plooze.core.utils;

public interface PloozeConstants {

	String[] DEFAULT_FIELDS = { "id_diffusion", "titre", "accroche_programme" };
	String EXTENSION = ".mp4";
	String FIELDS_SEPARATOR = ",";
	String DEFAULT_FILENAME_FORMAT = "${titre} - ${soustitre}" + EXTENSION;

	String CONTENT_URL_PREFIX = "http://pluzz.webservices.francetelevisions.fr/pluzz/liste/type/replay/nb/10000/chaine/";
	String[] CONTENT_URLS = { CONTENT_URL_PREFIX + "france2", CONTENT_URL_PREFIX + "france3", CONTENT_URL_PREFIX + "france4", CONTENT_URL_PREFIX + "france5",
			CONTENT_URL_PREFIX + "franceo" };

	String DETAILS_URL__id = "http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=%s&catalogue=Pluzz";
	String MEDIUM_RESOLUTION = "704x396";
}
