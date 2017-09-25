package org.essembeh.plooze.core.utils;

public interface PloozeConstants {
	String[] DEFAULT_FIELDS = { JsonFields.ID_DIFFUSION, JsonFields.TITRE, JsonFields.ACCROCHE };
	String EXTENSION = ".mp4";
	String OR = "|";
	String FIELDS_SEPARATOR = ",";
	String DEFAULT_OUTPUT_FORMAT = "@{" + JsonFields.TITRE + "}/@{" + JsonFields.SOUSTITRE + OR + JsonFields.ID_DIFFUSION + "}" + EXTENSION;
	String CONTENT_URL_PREFIX = "http://pluzz.webservices.francetelevisions.fr/pluzz/liste/type/replay/nb/10000/chaine/";
	String DETAILS_URL__id = "http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=%s&catalogue=Pluzz";
	String MEDIUM_RESOLUTION = "704x396";

	interface JsonFields {
		String URL = "url";
		String VIDEOS = "videos";
		String CHAINE_LABEL = "chaine_label";
		String FORMAT = "format";
		String GENRE_SIMPLIFIE = "genre_simplifie";
		String DUREE = "duree";
		String ACCROCHE = "accroche";
		String SOUSTITRE = "soustitre";
		String TITRE = "titre";
		String ID_DIFFUSION = "id_diffusion";
	}
}
