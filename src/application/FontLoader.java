package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import application.utils.ResourcePathUtil;

public class FontLoader {
	//src\resources\fonts
	//private static final String BASE_FONT_PATH = "src/resources/fonts/";
	
	public static File getFontFile(String fontName) throws FileNotFoundException, URISyntaxException {
		String base_font_path = ResourcePathUtil.getAppResourcePath("fonts").toString();
		
		String fontPath = base_font_path+ File.separator + fontName;
		File fontFile = new File(fontPath);
		if (!fontFile.exists()) {
			throw new FileNotFoundException("Font file not found: " + fontPath);			
		}
		return fontFile;
	}
}
