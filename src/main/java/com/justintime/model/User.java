package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User extends AbstractUser {
}
