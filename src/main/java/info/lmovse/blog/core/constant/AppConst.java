package info.lmovse.blog.core.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lmovse on 2017/3/3.
 * Tomorrow is a nice day.
 */
public class AppConst {
    public static Map<String, String> initConfig = new HashMap<>();

    public static final int PAGE_SIZE = 12;

    public static final boolean IS_REDIS_ENABLE = true;

    public static final int COMMENT_PAGE_SIZE = 6;

    public static final String PAGE_SIZE_STRING = "12";

    public static String LOGIN_SESSION_KEY = "login_user";

    public static final String USER_IN_COOKIE = "S_L_ID";

    /**
     * aes加密加盐
     */
    public static String AES_SALT = "0123456789abcdef";

    /**
     * 最大获取文章条数
     */
    public static final int MAX_POSTS = 9999;

    /**
     * 最大页码
     */
    public static final int MAX_PAGE = 100;

    /**
     * 文章最多可以输入的文字数
     */
    public static final int MAX_TEXT_COUNT = 200000;

    /**
     * 文章标题最多可以输入的文字个数
     */
    public static final int MAX_TITLE_COUNT = 200;

    /**
     * 点击次数超过多少更新到数据库
     */
    public static final int HIT_EXCEED = 10;

    /**
     * 上传文件最大1M
     */
    public static Integer MAX_FILE_SIZE = 1048576;

}
