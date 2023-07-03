package io.plagov.rssfeedtonotion;

import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.plagov.rssfeedtonotion.dao.BlogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@SpringBootApplication
public class RssFeedToNotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RssFeedToNotionApplication.class, args).close();

        ApplicationContext context = SpringApplication.run(RssFeedToNotionApplication.class, args);

        BlogDao blogDao = context.getBean(BlogDao.class);
        var input = new SyndFeedInput();

        var allBlogs = blogDao.getAllBlogs();
        allBlogs.forEach(blog -> {
            try {
                var feedEntries = input.build(new XmlReader(new URL(blog.url()))).getEntries();
                var first = IntStream.range(0, feedEntries.size())
                        .filter(i -> feedEntries.get(i).getTitle().equals("Should You Save More to Retire Earlier?"))
                        .findFirst()
                        .orElseThrow();

            } catch (FeedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Autowired
    private DataSource dataSource;

    @Bean
    public BlogDao blogDao(JdbcTemplate jdbcTemplate) {
        return new BlogDao(jdbcTemplate);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        // Define your JdbcTemplate configuration here
        return new JdbcTemplate(dataSource);
    }
}
