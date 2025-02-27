package guess.controller;

import guess.domain.Language;
import guess.domain.answer.ErrorDetails;
import guess.dto.result.ResultDto;
import guess.service.AnswerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Answer controller.
 */
@RestController
@RequestMapping("/api/answer")
public class AnswerController {
    private final AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/answers")
    @ResponseStatus(HttpStatus.OK)
    public void addAnswer(@RequestParam int questionIndex, @RequestParam long answerId, HttpSession httpSession) {
        answerService.setAnswer(questionIndex, answerId, httpSession);
    }

    @GetMapping("/result")
    public ResultDto getResult(@RequestParam String language, HttpSession httpSession) {
        var result = answerService.getResult(httpSession);
        List<ErrorDetails> errorDetailsList = answerService.getErrorDetailsList(httpSession);

        return ResultDto.convertToDto(result, errorDetailsList, Language.getLanguageByCode(language));
    }
}
