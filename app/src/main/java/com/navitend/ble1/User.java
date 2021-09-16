package com.navitend.ble1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class User {
    public String name;
    public String email;
    public HashMap<String, SampleData> samples;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.samples = new HashMap<String, SampleData>();
    }
}
