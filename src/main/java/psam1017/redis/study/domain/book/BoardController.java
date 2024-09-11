package psam1017.redis.study.domain.book;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/boards")
@RestController
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("")
    public List<Board> getBoards(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return boardService.getBoards(page, size);
    }
}
