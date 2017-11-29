package info.lmovse.blog.core.web;

import info.lmovse.blog.AppApplication;
import info.lmovse.blog.core.mapper.CommentMapper;
import info.lmovse.blog.core.pojo.dto.CommentContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by lmovse on 2017/8/25.
 * Tomorrow is a nice day.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppApplication.class)
public class OneToOne {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void oneToOne() {
        CommentContent content = commentMapper.findContent();
        System.out.println(content.getContent());
    }

}
