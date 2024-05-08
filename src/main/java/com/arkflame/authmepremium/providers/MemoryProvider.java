package com.arkflame.authmepremium.providers;

import java.util.HashMap;
import java.util.Map;

public class MemoryProvider implements DataProvider {
    private final Map<String, Boolean> premiumMap;

    public MemoryProvider() {
        this.premiumMap = new HashMap<>();
    }

    @Override
    public Boolean getPremium(String name) {
        return premiumMap.get(name);
    }

    @Override
    public void setPremium(String name, boolean premium) {
        premiumMap.put(name, premium);
    }
}
