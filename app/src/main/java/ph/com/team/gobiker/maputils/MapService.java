package ph.com.team.gobiker.maputils;

/**
 * a couple of map services
 * @author TimC
 */
public final class MapService {
    public static double distanceBetweenTwoPoint(double srcLat, double srcLng, double desLat, double desLng) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(desLat - srcLat);
        double dLng = Math.toRadians(desLng - srcLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(srcLat))
                * Math.cos(Math.toRadians(desLat)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        double meterConversion = 1609;

        return (int) (dist * meterConversion);
    }

    public static double handleCaloriesComputation(double speed, float weight) {
        double msToMph = 2.23694;
        double met = 0;
        if(speed <= 5.5){
            met = 3.5;
        }
        else if(5.5 < speed && speed <= 9.4){
            met = 4;
        }
        else if(9.4 < speed && speed < 10){
            met = 5.8;
        }
        else if(10 <= speed && speed <= 11.9){
            met = 6.8;
        }
        else if(12 <= speed && speed <= 13.9){
            met = 8;
        }
        else if(14 <= speed && speed <= 15.9){
            met = 10;
        }
        else if(16 <= speed && speed <= 19){
            met = 12;
        }
        else if(speed > 19){
            met = 15.8;
        }
        return (((met * weight * 3.5)/200)/60);
    }

}
