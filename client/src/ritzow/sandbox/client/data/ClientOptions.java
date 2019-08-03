package ritzow.sandbox.client.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class ClientOptions {
	private static final Map<String, String> OPTIONS = loadOptions();
	
	private static Map<String, String> loadOptions() {
		try {
			return Files.lines(StandardClientProperties.OPTIONS_PATH, StandardCharsets.UTF_8)
				.filter(line -> !line.stripLeading().startsWith("#") && !line.isBlank())
				.collect(Collectors.toMap(
					line -> line.substring(0, line.indexOf('=')).strip(), 
					line -> line.substring(line.indexOf('=') + 1).strip()
				));	
		} catch(IOException e) {
			e.printStackTrace();
			return Map.of();
		}
	}
	
	protected static String get(String option, String defaultValue) {
		return OPTIONS.getOrDefault(option, defaultValue);
	}
	
	protected static <T> T get(String option, T defaultValue, Function<String, T> converter) {
		var val = OPTIONS.get(option);
		return val == null || val.equals("") ? defaultValue : converter.apply(val);
	}
}
