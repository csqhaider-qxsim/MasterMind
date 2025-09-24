import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageTesting
{
        public static void main(String args[]) throws Exception
        {
                SwiftBotAPI swiftbot = new SwiftBotAPI();
                BufferedImage img = swiftbot.takeStill(ImageSize.SQUARE_720x720);
                ImageIO.write(img, "jpg", new File("/data/home/pi/TestImage.jpg"));

                System.exit(1);
        }
}