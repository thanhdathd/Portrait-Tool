package images;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class Img {
    private static Map<Integer, Image> cache = new HashMap<>();

    public static Image creatImg(int i) {
        if (cache.containsKey(i)) {
            return cache.get(i);
        }


        String path = "/icons/icon" + i + ".png"; // tùy bạn đặt tên

        URL url = Img.class.getResource(path);

        if (url == null) {
            System.out.println("Image not found: " + path);
            return null;
        }
        Image img = new ImageIcon(url).getImage();
        cache.put(i, img);

        return img;
    }

}
