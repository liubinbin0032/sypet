import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Solution {

	public static Rectangle2D translate(Rectangle2D arg0, double arg1, double arg2) {
		AffineTransform v1 = AffineTransform.getTranslateInstance(arg1, arg2);
		Shape v2 = v1.createTransformedShape(arg0);
		Rectangle2D v3 = v2.getBounds2D();
		return v3;
    }

}
