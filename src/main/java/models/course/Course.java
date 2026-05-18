package models.course;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {
    private final String id;
    private final String code;
    private final String name;
    private final String sectionPrefix;

    @JsonCreator
    public Course(
            @JsonProperty("id")             String id,
            @JsonProperty("code")           String code,
            @JsonProperty("name")           String name,
            @JsonProperty("sectionPrefix")  String sectionPrefix
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sectionPrefix = sectionPrefix;
    }

    public String getId() { return id; }

    public String getCode() { return code; }

    public String getName() { return name; }

    public String getSectionPrefix() { return sectionPrefix; }
}
