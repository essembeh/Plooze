package org.essembeh.plooze.core.utils;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.Episode;

public class EpisodeFilter implements Predicate<Episode> {

	private final String[] fields;
	private String motif = StringUtils.EMPTY;
	private Optional<Integer> durationMin = Optional.empty();
	private Optional<Integer> durationMax = Optional.empty();

	public EpisodeFilter(String[] fields) {
		this.fields = fields;
	}

	@Override
	public boolean test(Episode ep) {
		if (StringUtils.isNotBlank(motif)) {
			if (Stream.of(fields).noneMatch(field -> StringUtils.containsIgnoreCase(ep.getProperty(field), motif))) {
				return false;
			}
		}
		if (durationMin.map(d -> ep.getDuration() < d).orElse(false)) {
			return false;
		}
		if (durationMax.map(d -> ep.getDuration() > d).orElse(false)) {
			return false;
		}
		return true;
	}

	public EpisodeFilter setMotif(String motif) {
		this.motif = motif;
		return this;
	}

	public EpisodeFilter setDurationMax(int durationMax) {
		this.durationMax = Optional.of(durationMax);
		return this;
	}

	public EpisodeFilter setDurationMin(int durationMin) {
		this.durationMin = Optional.of(durationMin);
		return this;
	}
}
