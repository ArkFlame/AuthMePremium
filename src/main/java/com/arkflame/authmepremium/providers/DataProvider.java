package com.arkflame.authmepremium.providers;

public interface DataProvider {
    public Boolean getPremium(String name);

    public void setPremium(String name, boolean premium);
}
