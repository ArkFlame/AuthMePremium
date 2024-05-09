package com.arkflame.authmepremium.providers;

import java.util.HashMap;
import java.util.Map;

public class MemoryProvider implements DataProvider {
    private final Map<String, Boolean> premiumMap;
    private final Map<String, Boolean> premiumUUIDMap;

    public MemoryProvider() {
        this.premiumMap = new HashMap<>();
        this.premiumUUIDMap = new HashMap<>();
    }

    @Override
    public Boolean getPremium(String name) {
        return premiumMap.get(name);
    }

    @Override
    public void setPremium(String name, boolean premium) {
        premiumMap.put(name, premium);
    }

    @Override
    public void clear(String name) {
        premiumMap.remove(name);
    }

    @Override
    public Boolean getPremiumUUID(String name) {
        return premiumUUIDMap.get(name);
    }

    @Override
    public void setPremiumUUID(String name, boolean premiumUUID) {
        premiumUUIDMap.put(name, premiumUUID);
    }

    @Override
    public void clear() {
        premiumMap.clear();
    }
}
