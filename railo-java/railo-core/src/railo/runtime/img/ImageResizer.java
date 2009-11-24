
package railo.runtime.img;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import railo.runtime.exp.ExpressionException;
import railo.runtime.img.interpolation.Bessel;
import railo.runtime.img.interpolation.Blackman;
import railo.runtime.img.interpolation.Hamming;
import railo.runtime.img.interpolation.Hanning;
import railo.runtime.img.interpolation.Hermite;
import railo.runtime.img.interpolation.Interpolation;
import railo.runtime.img.interpolation.Lanczos;
import railo.runtime.img.interpolation.Mitchell;
import railo.runtime.img.interpolation.Quadratic;
import railo.runtime.img.interpolation.Triangle;


public class ImageResizer	{
    
    private static class ContributionInfo {
	int pixel;
	double weight;
	
	public ContributionInfo() {
	    /* empty */
	}
	
	public ContributionInfo(int pixel, double weight) {
	    this.pixel = pixel;
	    this.weight = weight;
	}
	
	public void setPixel(int pixel) {
	    this.pixel = pixel;
	}
	
	public int getPixel() {
	    return pixel;
	}
	
	public void setWeight(double weight) {
	    this.weight = weight;
	}
	
	public double getWeight() {
	    return weight;
	}
    }
    
    private static int horizontal(BufferedImage source, BufferedImage destination, double xFactor,Interpolation ip, double blur, ContributionInfo[] contribution) {
	if (source.getWidth() == destination.getWidth()
	    && source.getHeight() == destination.getHeight()) {
	    destination.setData(source.getData());
	    return destination.getWidth();
	}
	double scale = blur * Math.max(1.0 / xFactor, 1.0);
	double support = Math.max(scale * ip.getSupport(), 0.5);
	if (support <= 0.5) {
	    support = 0.500000000001;
	    scale = 1.0;
	}
	ColorModel cm = ColorModel.getRGBdefault();
	for (int x = 0; x < destination.getWidth(); x++) {
	    double center = x / xFactor;
	    int start = (int) Math.max(center - support + 0.5, 0.0);
	    int end = (int) Math.min(center + support + 0.5,
				     source.getWidth());
	    int n = setContributionWeight(start, end, contribution, ip,
					  center, scale);
	    int sourceWidth = (contribution[n - 1].getPixel()
			       - contribution[0].getPixel() + 1);
	    int[] sourcePixels = new int[sourceWidth * source.getHeight()];
	    int[] destPixels = new int[destination.getHeight()];
	    sourcePixels = source.getRGB(contribution[0].getPixel(), 0,
					 sourceWidth, source.getHeight(),
					 sourcePixels, 0, sourceWidth);
	    int destIndex = 0;
	    for (int y = 0; y < destination.getHeight(); y++) {
		double blue = 0.0;
		double green = 0.0;
		double red = 0.0;
		double opacity = 0.0;
		int[] c = new int[4];
		for (int i = 0; i < n; i++) {
		    int j = (y * (contribution[n - 1].getPixel()
				  - contribution[0].getPixel() + 1)
			     + (contribution[i].getPixel()
				- contribution[0].getPixel()));
		    c = cm.getComponents(sourcePixels[j], c, 0);
		    red += contribution[i].getWeight() * c[0];
		    green += contribution[i].getWeight() * c[1];
		    blue += contribution[i].getWeight() * c[2];
		    opacity += contribution[i].getWeight() * c[3];
		}
		red = red < 0.0 ? 0.0 : red > 255.0 ? 255.0 : red + 0.5;
		green
		    = green < 0.0 ? 0.0 : green > 255.0 ? 255.0 : green + 0.5;
		blue = blue < 0.0 ? 0.0 : blue > 255.0 ? 255.0 : blue + 0.5;
		opacity = (opacity < 0.0 ? 0.0 : opacity > 255.0 ? 255.0
			   : opacity + 0.5);
		destPixels[destIndex]
		    = cm.getDataElement(new int[] { (int) red, (int) green,
						    (int) blue,
						    (int) opacity },
					0);
		destIndex++;
	    }
	    destination.setRGB(x, 0, 1, destination.getHeight(), destPixels, 0,
			       1);
	}
	return destination.getWidth();
    }
    
    private static int setContributionWeight(int start, int end, ContributionInfo[] contribution, Interpolation interpolation, double center, double scale) {
		int n = 0;
		double density = 0.0;
		for (int i = start; i < end; i++) {
		    contribution[n].setPixel(i);
		    contribution[n].setWeight(interpolation.f((i - center + 0.5D)
						       / scale) / scale);
		    density += contribution[n].getWeight();
		    n++;
		}
		density = density == 0.0 ? 1.0 : 1.0 / density;
		for (int i = 0; i < n; i++)
		    contribution[i].setWeight(contribution[i].getWeight() * density);
		return n;
    }
    
    private static int vertical(BufferedImage source, BufferedImage destination, double y_factor, Interpolation ip, double blur, ContributionInfo[] contribution) {
	/*if (source.getWidth() == destination.getWidth()
	    && source.getHeight() == destination.getHeight()) {
	    destination.setData(source.getData());
	    return destination.getWidth();
	}*/
	double scale = blur * Math.max(1.0 / y_factor, 1.0);
	double support = Math.max(scale * ip.getSupport(), 0.5);
	if (support <= 0.5) {
	    support = 0.500000000001;
	    scale = 1.0;
	}
	ColorModel cm = ColorModel.getRGBdefault();
	for (int y = 0; y < destination.getHeight(); y++) {
	    double center =  y / y_factor;
	    int start = (int) Math.max(center - support + 0.5, 0.0);
	    int end = (int) Math.min(center + support + 0.5,
				     source.getHeight());
	    int n = setContributionWeight(start, end, contribution, ip,
					  center, scale);
	    int sourceHeight = (contribution[n - 1].getPixel()
				- contribution[0].getPixel() + 1);
	    int[] sourcePixels = new int[source.getWidth() * sourceHeight];
	    int[] destPixels = new int[destination.getWidth()];
	    sourcePixels = source.getRGB(0, contribution[0].getPixel(),
					 source.getWidth(), sourceHeight,
					 sourcePixels, 0, source.getWidth());
	    int destIndex = 0;
	    for (int x = 0; x < destination.getWidth(); x++) {
		double blue = 0.0;
		double green = 0.0;
		double red = 0.0;
		double opacity = 0.0;
		int[] c = new int[4];
		for (int i = 0; i < n; i++) {
		    int j = ((contribution[i].getPixel()
			      - contribution[0].getPixel()) * source.getWidth()
			     + x);
		    c = cm.getComponents(sourcePixels[j], c, 0);
		    red += contribution[i].getWeight() * c[0];
		    green += contribution[i].getWeight() * c[1];
		    blue += contribution[i].getWeight() * c[2];
		    opacity += contribution[i].getWeight() * c[3];
		}
		red = red < 0.0 ? 0.0 : red > 255.0 ? 255.0 : red + 0.5;
		green
		    = green < 0.0 ? 0.0 : green > 255.0 ? 255.0 : green + 0.5;
		blue = blue < 0.0 ? 0.0 : blue > 255.0 ? 255.0 : blue + 0.5;
		opacity = (opacity < 0.0 ? 0.0 : opacity > 255.0 ? 255.0
			   : opacity + 0.5);
		destPixels[destIndex]
		    = cm.getDataElement(new int[] { (int) red, (int) green,
						    (int) blue,
						    (int) opacity },
					0);
		destIndex++;
	    }
	    destination.setRGB(0, y, destination.getWidth(), 1, destPixels, 0,
			       destination.getWidth());
	}
	return destination.getHeight();
    }
    
    public static BufferedImage scale(BufferedImage image, double scale,int filterType, double blur) throws ExpressionException {
		if (scale == 0.0)
		    throw new ExpressionException("invalid size for image");
		int columns = (int) (image.getWidth() * scale);
		int rows = (int) ( image.getHeight() * scale);
		BufferedImage resizeImage = createBufferedImage(image, columns, rows);
		if (scale == 1.0) {
		    resizeImage.setData(image.getData());
		    return resizeImage;
		}
		Interpolation filter = getInterpolation(filterType);
		double scaleInverse = blur * Math.max(1.0 / scale, 1.0);
		double support = Math.max(scaleInverse * filter.getSupport(), 0.5);
		support
		    = support < filter.getSupport() ? filter.getSupport() : support;
		ContributionInfo[] contribution
		    = new ContributionInfo[(int) support * 2 + 3];
		for (int cloop = 0; cloop < (int) support * 2 + 3; cloop++)
		    contribution[cloop] = new ContributionInfo();
		BufferedImage sourceImage
		    = createBufferedImage(image, image.getWidth(), rows);
		int status = vertical(image, sourceImage, scale, filter, blur,
					    contribution);
		status |= horizontal(sourceImage, resizeImage, scale, filter, blur, contribution);
		if (status == 0)
		    throw new ExpressionException("invalid size for image");
		return resizeImage;
    }
    
    public static BufferedImage resize(BufferedImage image, int columns, int rows, int interpolation, double blur) throws ExpressionException {
	if (columns == 0 || rows == 0)
	    throw new ExpressionException("invalid size for image");
	BufferedImage resizeImage = createBufferedImage(image, columns, rows);
	/*fails
	 * if (columns == image.getWidth() && rows == image.getHeight()) {
	    resizeImage.setData(image.getData());
	    return resizeImage;
	}*/
	Interpolation filter = getInterpolation(interpolation);
	double x_factor = (double) columns / (double) image.getWidth();
	double scale = blur * Math.max(1.0 / x_factor, 1.0);
	double x_support = Math.max(scale * filter.getSupport(), 0.5);
	double y_factor = (double) rows / (double) image.getHeight();
	scale = blur * Math.max(1.0 / y_factor, 1.0);
	double y_support = Math.max(scale * filter.getSupport(), 0.5);
	double support = Math.max(x_support, y_support);
	if (support < filter.getSupport())
	    support = filter.getSupport();
	ContributionInfo[] contribution
	    = new ContributionInfo[(int) support * 2 + 3];
	for (int cloop = 0; cloop < (int) support * 2 + 3; cloop++)
	    contribution[cloop] = new ContributionInfo();
	int status;
	if (columns * (image.getHeight() + rows)
	    < rows * (image.getWidth() + columns)) {
	    BufferedImage sourceImage
		= createBufferedImage(image, columns, image.getHeight());
	    status = horizontal(image, sourceImage, x_factor, filter,
				      blur, contribution);
	    status |= vertical(sourceImage, resizeImage, y_factor,
				     filter, blur, contribution);
	} else {
	    BufferedImage sourceImage
		= createBufferedImage(image, image.getWidth(), rows);
	    status = vertical(image, sourceImage, y_factor, filter, blur,
				    contribution);
	    status |= horizontal(sourceImage, resizeImage, x_factor,
				       filter, blur, contribution);
	}
	if (status == 0)
	    throw new ExpressionException("can't resize image");
	return resizeImage;
    }
    
    private static BufferedImage createBufferedImage(BufferedImage imgSrc, int columns, int rows) {
    	ColorModel cm = imgSrc.getColorModel();
    	if (cm instanceof IndexColorModel) {
    		if (cm.getTransparency() != 1)	return new BufferedImage(columns, rows, 2);
    		return new BufferedImage(columns, rows, 1);
    	} 
    	return  (new BufferedImage(cm,imgSrc.getRaster().createCompatibleWritableRaster(columns,rows),cm.isAlphaPremultiplied(), null));
    }
    
    
    private static Interpolation getInterpolation(int filter) throws ExpressionException {
	//print.out(filter);
    switch (filter) {
	
		case 0:	return new Triangle();
		case Image.IP_HERMITE:	return new Hermite();
		case Image.IP_HANNING:	return new Hanning();
		case Image.IP_MEDIUMQUALITY:
		case Image.IP_HIGHPERFORMANCE:
		case Image.IP_HAMMING:	return new Hamming();
		case Image.IP_BLACKMAN:	return new Blackman();
		case Image.IP_QUADRATIC:return new Quadratic();
		case Image.IP_HIGHQUALITY:
		case Image.IP_MEDIUMPERFORMANCE:
		case Image.IP_MITCHELL:	return new Mitchell();
		case Image.IP_HIGHESTQUALITY:
		case Image.IP_LANCZOS:	return new Lanczos();
		case Image.IP_BESSEL:	return new Bessel();
		default:	throw new ExpressionException("invalid interpolation definition");
	}
    }
    
    
}
