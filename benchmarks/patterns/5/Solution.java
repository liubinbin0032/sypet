import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Solution {

    public static Rectangle2D rotateQuadrant(Rectangle2D arg0, int arg1) {
        Shape sypet_var53 = sypet_arg0;
		AffineTransform sypet_var54 = AffineTransform.getQuadrantRotateInstance(sypet_arg1);
		Shape sypet_var55 = sypet_var54.createTransformedShape(sypet_var53);
		Area sypet_var56 = new Area(sypet_var55);
		Rectangle2D sypet_var57 = sypet_var56.getBounds2D();
		return sypet_var57;
    }
}