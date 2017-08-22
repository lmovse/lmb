package info.lmovse.blog.core.web;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lmovse on 2017/8/19.
 * Tomorrow is a nice day.
 */
public class MultiThread {

    @Test
    public void originTest() {
        Thread threadOne = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println(i);
                System.out.println(i + 1);
            }
        });

        Thread threadTwo = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println(i);
                System.out.println(i + 1);
            }
        });

        threadOne.start();
        threadTwo.start();
    }

}
