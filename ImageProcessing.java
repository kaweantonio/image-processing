import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.xml.transform.Source;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImageProcessing extends JFrame {
    BufferedImage originalImage, img1, img2;
    JPanel imagePanel = new JPanel();
    JLabel img1lbl = new JLabel();
    JLabel img2lbl = new JLabel();
    
    ImageProcessing(){
        super("Filtro de Imagens");

        readImage("lena.png");
        img2 = toSepia(img1);
        img2lbl.setIcon(new ImageIcon(img2));
        imagePanel.add(img1lbl, BorderLayout.WEST);
        imagePanel.add(img2lbl, BorderLayout.EAST);
        
        add(imagePanel, BorderLayout.NORTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main (String[] args) {
        new ImageProcessing();
    }

    private void readImage(String path){
        try {
            originalImage = ImageIO.read(new File(path));
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Something get wrong while trying read the image: " + e.toString());
            System.exit(0);
        }
        img1 = copyImage(originalImage);
        img1lbl.setIcon(new ImageIcon(img1));
    }

    BufferedImage copyImage(BufferedImage source){
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    /* grayscale filter
        grayscale image achieved by using color's luminance, 
        where luminance is calculated from a RGB value
        using the following formula: L = .0299 * R + 0.587 * G + 0.114 * B.

        Source: (MSDN) https://msdn.microsoft.com/en-us/library/bb332387.aspx#tbconimagecolorizer_grayscaleconversion
    */ 
    BufferedImage toGrayscale(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue, gray; 
        
        rows = image.getHeight();
        columns = image.getWidth();

        BufferedImage gsImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        for (i = 0; i < rows - 1; i++){
            for (j = 0; j < columns - 1; j++){
                pixel = image.getRGB(i, j);
                
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = pixel & 0xff;

                gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                
                gsImage.setRGB(i, j, (gray << 16) + (gray << 8) + gray);
            }
        }

        return gsImage;
    }

    BufferedImage toSepia(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue;
        int outputRed, outputGreen, outputBlue; 
        
        rows = image.getHeight();
        columns = image.getWidth();

        BufferedImage spImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        for (i = 0; i < rows - 1; i++){
            for (j = 0; j < columns - 1; j++){
                pixel = image.getRGB(i, j);
                
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = pixel & 0xff;

                // convert RGB values to sepia
                outputRed = (int) (.393 * red + .769 * green + .189 * blue);
                outputGreen = (int) (.349 * red + .686 * green + .168 * blue);
                outputBlue = (int) (.272 * red + .534 * green + .131 * blue);  
                
                // if any output values is greater than 255, set it to 255
                outputRed = (outputRed > 255) ? 255 : outputRed;
                outputGreen = (outputGreen > 255) ? 255 : outputGreen;
                outputBlue = (outputBlue > 255) ? 255 : outputBlue;
                
                spImage.setRGB(i, j, (outputRed << 16) + (outputGreen << 8) + outputBlue);
            }
        }

        return spImage;
    }
}
