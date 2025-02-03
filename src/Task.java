import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int id = 0;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = Status.NEW;
    }

    public void setId(int id) {
        if (this.id == 0) {
            this.id = id;
        }
    }

    public int getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (this.id == 0 || task.id == 0) {
            return Objects.equals(this.name, task.name) && Objects.equals(this.description, task.description);
        } else {
            return id == task.id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    @Override
    public String toString() {
        return "Task{id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}