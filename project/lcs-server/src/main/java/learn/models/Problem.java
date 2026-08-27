package learn.models;

import java.util.Objects;

public class Problem {
    private int id;
    private ProblemCategory category;
    private String subcategory;
    private String description;

    public Problem() {
    }

    public Problem(ProblemCategory category, String subcategory, String description) {
        this(0, category, subcategory, description);
    }

    public Problem(int id, ProblemCategory category, String subcategory, String description) {
        this.id = id;
        this.category = category;
        this.subcategory = subcategory;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ProblemCategory getCategory() {
        return category;
    }

    public void setCategory(ProblemCategory category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Problem problem = (Problem) o;
        return id == problem.id
                && category == problem.category
                && Objects.equals(subcategory, problem.subcategory)
                && Objects.equals(description, problem.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, subcategory, description);
    }
}
