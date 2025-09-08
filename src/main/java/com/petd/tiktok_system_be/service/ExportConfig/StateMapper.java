package com.petd.tiktok_system_be.service.ExportConfig;

import java.util.HashMap;
import java.util.Map;

public class StateMapper {

    private static final Map<String, String> STATE_MAP = new HashMap<>();

    static {
        STATE_MAP.put("alabama", "AL");
        STATE_MAP.put("alaska", "AK");
        STATE_MAP.put("arizona", "AZ");
        STATE_MAP.put("arkansas", "AR");
        STATE_MAP.put("california", "CA");
        STATE_MAP.put("colorado", "CO");
        STATE_MAP.put("connecticut", "CT");
        STATE_MAP.put("delaware", "DE");
        STATE_MAP.put("florida", "FL");
        STATE_MAP.put("georgia", "GA");
        STATE_MAP.put("hawaii", "HI");
        STATE_MAP.put("idaho", "ID");
        STATE_MAP.put("illinois", "IL");
        STATE_MAP.put("indiana", "IN");
        STATE_MAP.put("iowa", "IA");
        STATE_MAP.put("kansas", "KS");
        STATE_MAP.put("kentucky", "KY");
        STATE_MAP.put("louisiana", "LA");
        STATE_MAP.put("maine", "ME");
        STATE_MAP.put("maryland", "MD");
        STATE_MAP.put("massachusetts", "MA");
        STATE_MAP.put("michigan", "MI");
        STATE_MAP.put("minnesota", "MN");
        STATE_MAP.put("mississippi", "MS");
        STATE_MAP.put("missouri", "MO");
        STATE_MAP.put("montana", "MT");
        STATE_MAP.put("nebraska", "NE");
        STATE_MAP.put("nevada", "NV");
        STATE_MAP.put("new hampshire", "NH");
        STATE_MAP.put("new jersey", "NJ");
        STATE_MAP.put("new mexico", "NM");
        STATE_MAP.put("new york", "NY");
        STATE_MAP.put("north carolina", "NC");
        STATE_MAP.put("north dakota", "ND");
        STATE_MAP.put("ohio", "OH");
        STATE_MAP.put("oklahoma", "OK");
        STATE_MAP.put("oregon", "OR");
        STATE_MAP.put("pennsylvania", "PA");
        STATE_MAP.put("rhode island", "RI");
        STATE_MAP.put("south carolina", "SC");
        STATE_MAP.put("south dakota", "SD");
        STATE_MAP.put("tennessee", "TN");
        STATE_MAP.put("texas", "TX");
        STATE_MAP.put("utah", "UT");
        STATE_MAP.put("vermont", "VT");
        STATE_MAP.put("virginia", "VA");
        STATE_MAP.put("washington", "WA");
        STATE_MAP.put("west virginia", "WV");
        STATE_MAP.put("wisconsin", "WI");
        STATE_MAP.put("wyoming", "WY");
        STATE_MAP.put("washington d.c.", "DC");
        STATE_MAP.put("american samoa", "AS");
        STATE_MAP.put("guam", "GU");
        STATE_MAP.put("northern mariana islands", "MP");
        STATE_MAP.put("puerto rico", "PR");
        STATE_MAP.put("u.s. minor outlying islands", "UM");
        STATE_MAP.put("u.s. virgin islands", "VI");
    }

    public static String getAbbreviation(String stateName) {
        if (stateName == null) return null;
        return STATE_MAP.get(stateName.toLowerCase().trim());
    }
}
