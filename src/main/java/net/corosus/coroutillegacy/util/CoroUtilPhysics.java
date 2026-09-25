package net.corosus.coroutillegacy.util;

import java.util.List;


public class CoroUtilPhysics {


    public static boolean isInConvexShape(Vec3 test, List<Vec3> points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            Vec3 vecI = points.get(i);
            Vec3 vecJ = points.get(j);
            if ((vecI.zCoord > test.zCoord) != (vecJ.zCoord > test.zCoord) &&
                    (test.xCoord < (vecJ.xCoord - vecI.xCoord) * (test.zCoord - vecI.zCoord) / (vecJ.zCoord - vecI.zCoord) + vecI.xCoord)) {
                result = !result;
            }
        }
        return result;
    }


    public static double getDistanceToShape(Vec3 point, List<Vec3> points) {
        float closestDist1 = 9999;
        float closestDist2 = 9999;

        Vec3 closestPoint1 = null;
        Vec3 closestPoint2 = null;


        for (int i = 0; i < 2; i++) {
            for (Vec3 pointTest : points) {
                double dist = pointTest.distanceTo(point);

                if (dist < closestDist1) {
                    closestDist1 = (float) dist;
                    closestPoint1 = pointTest;
                } else if (dist < closestDist2 && pointTest != closestPoint1) {
                    closestDist2 = (float) dist;
                    closestPoint2 = pointTest;
                }
            }
        }

        if (closestPoint1 == null || closestPoint2 == null) {

            return -1;
        }

        return distBetweenPointAndLine(point.xCoord, point.zCoord, closestPoint1.xCoord, closestPoint1.zCoord, closestPoint2.xCoord, closestPoint2.zCoord);
    }


    public static double distBetweenPointAndLine(double x, double y, double x1, double y1, double x2, double y2) {


        double AB = distBetween(x, y, x1, y1);
        double BC = distBetween(x1, y1, x2, y2);
        double AC = distBetween(x, y, x2, y2);


        double s = (AB + BC + AC) / 2;
        double area = Math.sqrt(s * (s - AB) * (s - BC) * (s - AC));


        double AD = (2 * area) / BC;
        return AD;
    }

    public static double distBetween(double x, double y, double x1, double y1) {
        double xx = x1 - x;
        double yy = y1 - y;

        return Math.sqrt(xx * xx + yy * yy);
    }

}