package com.wei.create.prototype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Cloneable {
    private String username;
    private Integer age;

    @Override
    protected User clone() throws CloneNotSupportedException {
        User user = new User();
        user.setUsername(this.username);
        user.setAge(this.age);
        return user;
    }
}
