package org.ak.gitanalyzer.step2.data;

import java.util.Comparator;

/**
 * Created by Andrew on 01.10.2016.
 */
public class Author {

    final String email;
    final String name;

    String location;
    String team;

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

        if (!email.equals(author.email)) return false;
        return name.equals(author.name);

    }

    @Override
    public int hashCode() {
        String email = this.email == null ? "" : this.email;
        int result = email.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public static class NameEmailComparator implements Comparator<Author> {
        @Override
        public int compare(Author o1, Author o2) {
            int result = (o1.getName() == null ? "" : o1.getName()).compareTo(o2.getName() == null ? "" : o2.getName());
            if (result == 0) {
                result = (o1.getEmail() == null ? "" : o1.getEmail()).compareTo(o2.getEmail() == null ? "" : o2.getEmail());
            }
            return result;
        }
    }
}
