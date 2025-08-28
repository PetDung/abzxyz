package com.petd.tiktok_system_be.constant;

public enum Role {

    Admin("role/Admin"),
    Employee("role/member"),
    Leader("role/leader"),;

    private final String role;

    Role(String role){
        this.role = role;
    }

    public static Role fromValue(String input) {
        for (Role r : values()) {
            if (r.role.equalsIgnoreCase(input)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + input);
    }
}
