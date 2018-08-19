import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImageProcessing extends JFrame {
    BufferedImage originalImage, filteredImage;
    JPanel imagesPanel = new JPanel();
    JPanel optionPanel = new JPanel();
    JLabel originaImageLabel = new JLabel();
    JLabel filteredImageLabel = new JLabel();
    JComboBox filtersList;
    
    ImageProcessing(){
        super("Filtros de Imagem");

        readImage("lena.png");
        // readImage("waterlilies.jpg");
        filteredImage = grayscale(originalImage);
        filteredImageLabel.setIcon(new ImageIcon(filteredImage));
        imagesPanel.add(originaImageLabel, BorderLayout.WEST);
        imagesPanel.add(filteredImageLabel, BorderLayout.EAST);
        
        String[] filters = {"Grayscale", "Sepia", "Solarize", "Gaussian Blur", "Laplace Edge Detector"};
        filtersList = new JComboBox<String>(filters);
        filtersList.setSelectedIndex(0);
        optionPanel.add(filtersList);

        add(imagesPanel, BorderLayout.NORTH);
        add(optionPanel, BorderLayout.SOUTH);

        filtersList.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                changeFilter();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
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
        originaImageLabel.setIcon(new ImageIcon(originalImage));
    }

    // BufferedImage copyImage(BufferedImage source){
    //     BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
    //     Graphics2D g = copy.createGraphics();
    //     g.drawImage(source, 0, 0, null);
    //     g.dispose();
    //     return copy;
    // }

    void changeFilter(){
        int filter = filtersList.getSelectedIndex();

        switch(filter){
            case 0: filteredImage = grayscale(originalImage); break;
            case 1: filteredImage = sepia(originalImage); break;
            case 2: filteredImage = solarize(originalImage); break;
            case 3: filteredImage = gaussianBlur(originalImage); break;
            case 4: filteredImage = laplaceEdgeDetector(originalImage); break;
        }

        filteredImageLabel.setIcon(new ImageIcon(filteredImage));
    }

    /* grayscale filter
        grayscale image achieved by using color's luminance, 
        where luminance is calculated from a RGB value
        using the following formula: L = .0299 * R + 0.587 * G + 0.114 * B.

        Source: (MSDN) https://msdn.microsoft.com/en-us/library/bb332387.aspx#tbconimagecolorizer_grayscaleconversion
    */ 
    BufferedImage grayscale(BufferedImage image){
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
    BufferedImage sepia(BufferedImage image){
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

        BufferedImage szImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

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
                
                szImage.setRGB(i, j, (red << 16) + (green << 8) + blue);
            }
        }

        return szImage;
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
        
        BufferedImage laplaceImage = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);

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
                
                laplaceImage.setRGB(i, j, (red << 16) + (green << 8) + blue);
            }
        }

        return laplaceImage;
    }
}
