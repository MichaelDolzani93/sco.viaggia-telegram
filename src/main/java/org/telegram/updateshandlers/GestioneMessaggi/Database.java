package org.telegram.updateshandlers.GestioneMessaggi;

import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.model.TaxiContact;
import eu.trentorise.smartcampus.mobilityservice.model.TripData;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import org.telegram.telegrambots.api.objects.Location;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String AUTOBUS_ID = "12";
    private static final String TRAINS_ID_BV = "6";
    private static final String TRAINS_ID_TB = "5";
    private static final String TRAINS_ID_TM = "10";
    private static List<Parking> parkings = new ArrayList<>();
    private static List<Parking> bikeSharings = new ArrayList<>();
    private static List<TaxiContact> taxi = new ArrayList<>();
    private static List<Route> autobus = new ArrayList<>();
    private static List<Route> trains_BV = new ArrayList<>();
    private static List<Route> trains_TM = new ArrayList<>();
    private static List<Route> trains_TB = new ArrayList<>();
    private static List<Route> trains = new ArrayList<>();

    private static final String SERVER_URL = "https://tn.smartcommunitylab.it/core.mobility";
    private static MobilityDataService dataService = new MobilityDataService(SERVER_URL);

    private static String capitalize(String s) {
        String text = "";
        String[] words = s.split("\\s");
        for (String string : words)
            text += (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase() + " ");
        return text.substring(0, text.length()-1);
    }

    public static List<Parking> getParkings() throws SecurityException, MobilityServiceException {
        return parkings = dataService.getParkings("COMUNE_DI_TRENTO", null);
    }

    public static List<Parking> getBikeSharing() throws SecurityException, MobilityServiceException {
        return bikeSharings = dataService.getBikeSharings("BIKE_SHARING_TOBIKE_TRENTO", null);
    }

    public static List<TaxiContact> getTaxiInfo() throws SecurityException, MobilityServiceException {
        return taxi = dataService.getTaxiAgencyContacts(null);
    }

    public static List<Parking> getNear(List<Parking> zone, Location loc) {
        List<Parking> near = new ArrayList<>();

        for (Parking el : zone)
            if (DistanceCalculator.distance(loc.getLatitude(), loc.getLongitude(), el.getPosition()[0], el.getPosition()[1], "K") <= 1.5)
                near.add(el);
        return near;
    }

    public static List<Route> getAutbus() throws SecurityException, MobilityServiceException {
        return autobus = dataService.getRoutes(AUTOBUS_ID, null);
    }

    public static List<Route> getTrains() throws SecurityException, MobilityServiceException {
        trains.clear();
        trains_BV.clear();
        trains_TB.clear();
        trains_TM.clear();


        trains_BV.addAll(dataService.getRoutes(TRAINS_ID_BV, null));
        trains_TB.addAll(dataService.getRoutes(TRAINS_ID_TB, null));
        trains_TM.addAll(dataService.getRoutes(TRAINS_ID_TM, null));

        for (Route r : trains_BV) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));}


        for (Route r : trains_TB) {
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }

        for (Route r : trains_TM){
            r.setRouteLongName(addSpace(r.getRouteLongName()));
            r.setRouteLongName(capitalize(r.getRouteLongName()));
        }


        trains.addAll(trains_BV);
        trains.addAll(trains_TB);
        trains.addAll(trains_TM);

        return trains;
    }

    private static String addSpace(String s) {
        return s.replace("/", " / ").trim();
    }

    public static List<Stop> getStopAutobus(String autobusID) throws SecurityException, MobilityServiceException {
        Id id = stopAutobusId(autobusID);
        return id == null ? null : dataService.getStops(AUTOBUS_ID, id.getId(), null);
    }

    public static List<Stop> getStopTrain(String trainID) throws SecurityException, MobilityServiceException {
        Id id = stopTrainID(trainID);
        String agencyID = trainAgencyId(trainID);

        return id == null || agencyID == null ? null : dataService.getStops(agencyID, id.getId(), null);

    }

    public static List<TripData> getNextTrips(String agencyId, String stopId) throws MobilityServiceException {
        return dataService.getNextTrips(agencyId, stopId, 5, null);
    }

    private static Id stopAutobusId(String autobusID) {
        for (Route r : autobus)
            if (r.getRouteShortName().equals(autobusID))
                return r.getId();

        return null;
    }

    private static Id stopTrainID(String trainID) {
        for (Route r : trains)
            if (r.getRouteLongName().equals(trainID))
                return r.getId();

        return null;
    }

    private static String trainAgencyId(String autobusID) {
        for (Route r : trains_BV)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_BV;

        for (Route r : trains_TB)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_TB;

        for (Route r : trains_TM)
            if (r.getRouteLongName().equals(autobusID))
                return TRAINS_ID_TM;

        return null;
    }

}