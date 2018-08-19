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

        // readImage("lena.png");
        readImage("waterlilies.jpg");
        img2 = laplaceEdgeDetector(img1);
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

        for (i = 0; i < columns; i++){
            for (j = 0; j < rows; j++){
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

    // sepia filter
    BufferedImage toSepia(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue;
        int outputRed, outputGreen, outputBlue; 
        
        rows = image.getHeight();
        columns = image.getWidth();

        BufferedImage spImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        for (i = 0; i < columns; i++){
            for (j = 0; j < rows; j++){
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

    // solarize filter
    BufferedImage solarize(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue;
        int intensity, threshold = 140;
        
        rows = image.getHeight();
        columns = image.getWidth();

        BufferedImage solImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        for (i = 0; i < columns; i++){
            for (j = 0; j < rows; j++){
                pixel = image.getRGB(i, j);
                
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = pixel & 0xff;

                intensity = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                if (intensity < threshold) {
                    red = 255 - red;
                    green = 255 - green;
                    blue = 255 - blue;
                }
                
                solImage.setRGB(i, j, (red << 16) + (green << 8) + blue);
            }
        }

        return solImage;
    }

    BufferedImage gaussianBlur(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue;
        // variables to iterate the kernel
        int x, y, n, level;
        // 3x3 gaussian kernel
        int[][] kernel = {{1,2,1}, 
                          {2,4,2}, 
                          {1,2,1}}; 
        // variables to navigate the neighborhood pixels
        int xNeighborhood, yNeighborhood;
        
        rows = image.getHeight();
        columns = image.getWidth();
        n = kernel.length;
        level = n/2;

        BufferedImage blurredImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        // iterate each pixel in the image
        for (i = 0; i < columns; i++){
            for (j = 0; j < rows; j++){
                red = 0;
                green = 0;
                blue = 0;

                // iterate each pixel in the kernel
                for (x = 0; x < n; x++) {
                    for (y = 0; y < n; y++){
                        xNeighborhood = i + x - level;
                        yNeighborhood = j + y - level;

                        // checks if x and y are out of bounds of the image
                        if (xNeighborhood < 0 && yNeighborhood < 0){
                            xNeighborhood = columns - 1;
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood < 0 && yNeighborhood == rows){
                            xNeighborhood = columns - 1;
                            yNeighborhood = 0;
                        } else if (xNeighborhood == columns && yNeighborhood < 0){
                            xNeighborhood = 0;
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood == columns && yNeighborhood == rows){
                            xNeighborhood = 0;
                            yNeighborhood = 0;
                        } else if (xNeighborhood < 0 && yNeighborhood >= 0) {
                            xNeighborhood = columns - 1;
                        } else if (xNeighborhood == columns && yNeighborhood >= 0){
                            xNeighborhood = 0;
                        } else if (xNeighborhood >= 0 && yNeighborhood < 0){
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood >= 0 && yNeighborhood == rows){
                            yNeighborhood = 0;
                        }

                        pixel = image.getRGB(xNeighborhood, yNeighborhood);

                        red += ((pixel >> 16) & 0xff) * kernel[x][y] / 16;
                        green += ((pixel >> 8) & 0xff) * kernel[x][y] / 16;
                        blue += (pixel & 0xff) * kernel[x][y] / 16;
                    }
                }

                if (red > 255) red = 255;
                else if (red < 0) red = 0;

                if (green > 255) green = 255;
                else if (green < 0) green = 0;

                if (blue > 255) blue = 255;
                else if (blue < 0) blue = 0;
                
                blurredImage.setRGB(i, j, (red << 16) + (green << 8) + blue);
            }
        }

        return blurredImage;
    }

    BufferedImage laplaceEdgeDetector(BufferedImage image){
        int i, j, rows, columns, pixel, red, green, blue;
        // variables to iterate the kernel
        int x, y, n, level;
        // 3x3 laplacian kernel
        int[][] kernel = {{-1,-1,-1}, 
                          {-1,8,-1}, 
                          {-1,-1,-1}}; 
        // variables to navigate the neighborhood pixels
        int xNeighborhood, yNeighborhood;
        
        rows = image.getHeight();
        columns = image.getWidth();
        n = kernel.length;
        level = n/2;

        BufferedImage blurredImage = gaussianBlur(image);
        
        BufferedImage edgeImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

        // iterate each pixel in the image
        for (i = 0; i < columns; i++){
            for (j = 0; j < rows; j++){
                red = 0;
                green = 0;
                blue = 0;

                // iterate each pixel in the kernel
                for (x = 0; x < n; x++) {
                    for (y = 0; y < n; y++){
                        xNeighborhood = i + x - level;
                        yNeighborhood = j + y - level;

                        // checks if x and y are out of bounds of the image
                        if (xNeighborhood < 0 && yNeighborhood < 0){
                            xNeighborhood = columns - 1;
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood < 0 && yNeighborhood == rows){
                            xNeighborhood = columns - 1;
                            yNeighborhood = 0;
                        } else if (xNeighborhood == columns && yNeighborhood < 0){
                            xNeighborhood = 0;
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood == columns && yNeighborhood == rows){
                            xNeighborhood = 0;
                            yNeighborhood = 0;
                        } else if (xNeighborhood < 0 && yNeighborhood >= 0) {
                            xNeighborhood = columns - 1;
                        } else if (xNeighborhood == columns && yNeighborhood >= 0){
                            xNeighborhood = 0;
                        } else if (xNeighborhood >= 0 && yNeighborhood < 0){
                            yNeighborhood = rows - 1;
                        } else if (xNeighborhood >= 0 && yNeighborhood == rows){
                            yNeighborhood = 0;
                        }

                        pixel = blurredImage.getRGB(xNeighborhood, yNeighborhood);

                        red += ((pixel >> 16) & 0xff) * kernel[x][y];
                        green += ((pixel >> 8) & 0xff) * kernel[x][y];
                        blue += (pixel & 0xff) * kernel[x][y];
                    }
                }

                if (red > 255) red = 255;
                else if (red < 0) red = 0;

                if (green > 255) green = 255;
                else if (green < 0) green = 0;

                if (blue > 255) blue = 255;
                else if (blue < 0) blue = 0;
                
                edgeImage.setRGB(i, j, (red << 16) + (green << 8) + blue);
            }
        }

        return edgeImage;
    }
}
