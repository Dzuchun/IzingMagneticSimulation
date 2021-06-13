
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Settings {
	// JSON
	private static final JSONParser JSON_PARSER = new JSONParser();
	private static FileReader JSON_FILE_READER;
	private static JSONObject PROPERTIES_OBJECT;
	static {
		try {
			Settings.JSON_FILE_READER = new FileReader("settings.json");
			Settings.PROPERTIES_OBJECT = (JSONObject) Settings.JSON_PARSER.parse(Settings.JSON_FILE_READER);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find \"settings.json\" file. All values will be default.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Settings file might be damaged:");
			e.printStackTrace();
		}
	}
	// Data
	private static final JSONObject DATA_PROPERTIES = (JSONObject) Settings.PROPERTIES_OBJECT.get("data");
	public static final String SAVES_FORDER = (String) Settings.DATA_PROPERTIES.get("saves_folder");
	public static final String FILENAME_FORMAT = (String) Settings.DATA_PROPERTIES.get("filename_format");
	public static final long MACROSAVES = (long) Settings.DATA_PROPERTIES.get("macrosaves");
	public static final long MICROSAVES = (long) Settings.DATA_PROPERTIES.get("microsaves");
	// Start conditions
	private static final JSONObject START_CONDITIONS = (JSONObject) Settings.PROPERTIES_OBJECT.get("start_conditions");
	public static final double TEMPERATURE_0 = (double) Settings.START_CONDITIONS.get("temperature_0");
	public static final double TEMPERATURE_D = (double) Settings.START_CONDITIONS.get("temperature_d");
	public static final double TENSION_0 = (double) Settings.START_CONDITIONS.get("tension_0");
	public static final double TENSION_D = (double) Settings.START_CONDITIONS.get("tension_d");
	public static final int SIMULATIONS = (int) (long) Settings.START_CONDITIONS.get("simulations");
	public static final int MAX_THREADS = (int) (long) Settings.START_CONDITIONS.get("max_threads");
	// Mech
	private static final JSONObject MECH_PROPERTIES = (JSONObject) Settings.PROPERTIES_OBJECT.get("mech");
	public static final int GRID_SIZE = (int) (long) Settings.MECH_PROPERTIES.get("grid_size");
	public static final long ITERATIONS_DENSITY = (long) Settings.MECH_PROPERTIES.get("iterations_density");
	// Graph
	private static final JSONObject GRAPHICS_PROPERTIES = (JSONObject) Settings.PROPERTIES_OBJECT.get("graph");
	public static final long FPS = (long) Settings.GRAPHICS_PROPERTIES.get("fps");
	public static final boolean ENABLE_VISUALIZATION = (boolean) Settings.GRAPHICS_PROPERTIES
			.get("enable_visualization");
}
