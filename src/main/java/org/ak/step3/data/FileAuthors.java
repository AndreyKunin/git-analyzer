package org.ak.step3.data;

import org.ak.step2.data.Author;
import org.ak.step2.data.File;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andrew on 08.10.2016.
 */
public class FileAuthors implements Comparable<FileAuthors> {
    private File file;
    private int authorsCount;
    private Set<Author> authors = new HashSet<>();

    public FileAuthors(File file) {
        this.file = file;
    }

    public void addAuthor(Author author) {
        this.authors.add(author);
        this.authorsCount = this.authors.size();
    }

    public void addAuthors(Collection<Author> authors) {
        this.authors.addAll(authors);
        this.authorsCount = this.authors.size();
    }

    public File getFile() {
        return file;
    }

    public int getAuthorsCount() {
        return authorsCount;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    @Override
    public int compareTo(FileAuthors o) {
        return authorsCount < o.authorsCount ? 1 : authorsCount == o.authorsCount ? 0 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileAuthors that = (FileAuthors) o;

        return file.equals(that.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
