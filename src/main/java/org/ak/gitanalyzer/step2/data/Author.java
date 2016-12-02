package org.ak.gitanalyzer.step2.data;

import java.util.Comparator;

/**
 * Created by Andrew on 01.10.2016.
 */
public class Author {

    private final String email;
    private final String name;

    private int hashCode;

    private String location;
    private String team;

    public Author(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    void setLocation(String location) {
        if (location != null && location.length() == 0) {
            location = null;
        }
        this.location = location;
    }

    public String getTeam() {
        return team;
    }

    void setTeam(String team) {
        if (team != null && team.length() == 0) {
            team = null;
        }
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;
        String email = this.email == null ? "" : this.email;
        String name = this.name == null ? "" : this.name;

        if (!email.equals(author.email)) return false;
        return name.equals(author.name);

    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            String email = this.email == null ? "" : this.email;
            String name = this.name == null ? "" : this.name;
            int result = email.hashCode();
            hashCode = 31 * result + name.hashCode();
        }
        return hashCode;
    }

    public static class NameEmailComparator implements Comparator<Author> {
        @Override
        public int compare(Author o1, Author o2) {
            String name1 = o1.getName() == null ? "" : o1.getName().toLowerCase();
            String name2 = o2.getName() == null ? "" : o2.getName().toLowerCase();
            String email1 = o1.getEmail() == null ? "" : o1.getEmail().toLowerCase();
            String email2 = o2.getEmail() == null ? "" : o2.getEmail().toLowerCase();
            int result = name1.compareTo(name2);
            if (result == 0) {
                result = email1.compareTo(email2);
            }
            return result;
        }
    }
}
