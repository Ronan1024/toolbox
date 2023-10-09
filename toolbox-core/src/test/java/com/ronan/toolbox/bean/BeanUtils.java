package com.ronan.toolbox.bean;

import io.github.Ronan1024.toolbox.core.bean.BeanUtil;
import org.junit.Test;

/**
 * @author L.J.Ran
 * @version 1.0
 */
public class BeanUtils {


    @Test
    public void getFieldName() {
        System.out.println(BeanUtil.getFieldName(User::getName));
    }

    @Test
    public void getProperty() {
        User user = new User();
        user.setName("123");
        System.out.println(BeanUtil.getProperty(user, "name"));
    }

    public class User {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

