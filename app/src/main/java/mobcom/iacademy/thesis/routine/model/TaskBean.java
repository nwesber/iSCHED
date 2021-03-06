package mobcom.iacademy.thesis.routine.model;

public class TaskBean {

    private String id;
    private String title;
    private String content;
    private String dueDate;
    private String priority;
    private String routineGroup;
    private String routineId;
    private String routineName;
    private String username;
    private String timeStart;
    private Boolean isCompleted;
    private int photo;


    public TaskBean(String routineId, String routineGroup, String username){
        this.routineId = routineId;
        this.routineGroup = routineGroup;
        this.username = username;
    }

    public TaskBean(String id, String title, String content, String dueDate, String priority, String username, String timeStart, String routineId, String routineGroup) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.priority = priority;
        this.username = username;
        this.timeStart = timeStart;
        this.routineId = routineId;
        this.routineGroup = routineGroup;
    }

    public String getRoutineId() {
        return routineId;
    }

    public void setRoutineId(String routineId) {
        this.routineId = routineId;
    }

    public String getRoutineName() {
        return routineName;
    }

    public void setRoutineName(String routineName) {
        this.routineName = routineName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRoutineGroup() {
        return routineGroup;
    }

    public void setRoutineGroup(String routineGroup) {
        this.routineGroup = routineGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
