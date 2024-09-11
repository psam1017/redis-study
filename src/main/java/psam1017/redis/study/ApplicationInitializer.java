package psam1017.redis.study;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import psam1017.redis.study.domain.book.Board;
import psam1017.redis.study.domain.book.BoardRepository;
import psam1017.redis.study.domain.user.User;
import psam1017.redis.study.domain.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationInitializer {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public ApplicationInitializer(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initialize() {
        LocalDateTime now = LocalDateTime.now();
        List<Board> boards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            boards.add(new Board("title" + i, "content" + i, now));
        }
        boardRepository.saveAll(boards);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            users.add(new User("user" + i));
        }
        userRepository.saveAll(users);
    }
}
