package com.zerom.spring.batch.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class Authority {

    private String auth_group;
    private String auth_gorup_gims;
}
