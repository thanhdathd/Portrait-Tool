package core.state;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;

/**
 * Custom Adapter to save java.awt.Color as Hex string.
 */
public class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, Color value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        String hex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());
        out.value(hex);
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        String hex = in.nextString();
        return Color.decode(hex);
    }
}
