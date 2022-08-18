package de.uni_bremen.see.arborext;

import java.util.List;
import java.util.ArrayList;

public class Developer
{
    private int id;
    private String name;

    static protected int newId = 1;
    static protected List<Developer> developers = new ArrayList<Developer> ();

    protected Developer(final String name)
    {
        this.id = newId++;
        this.name = name;
        developers.add(this);
    }

    public String getId()
    {
        return "D" + Integer.toString(this.id);
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Developer)) {
            return false;
        }

        Developer dev = (Developer) obj;
        return this.name.equals(dev.getName());
    }

    static public Developer probeDeveloper(final String name)
    {
        for (Developer dev : developers) {
            if (dev.getName().equals(name)) {
                return dev;
            }
        }

        Developer dev = new Developer(name);
        return dev;
    }

    static public List<Developer> getDevelopers()
    {
        return developers;
    }
}
