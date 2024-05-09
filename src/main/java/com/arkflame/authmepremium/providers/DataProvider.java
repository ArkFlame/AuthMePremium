package com.arkflame.authmepremium.providers;

public interface DataProvider {
    public Boolean getPremium(String name);

    public void setPremium(String name, boolean premium);

    public Boolean getPremiumUUID(String name);

    public void setPremiumUUID(String name, boolean premiumUUID);

    public void clear(String name);

    public void clear();
}
