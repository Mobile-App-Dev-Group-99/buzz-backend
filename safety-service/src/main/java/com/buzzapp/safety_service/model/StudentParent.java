package com.buzzapp.safety_service.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students_parents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentParent {

    @EmbeddedId
    private StudentParentId id;
}
