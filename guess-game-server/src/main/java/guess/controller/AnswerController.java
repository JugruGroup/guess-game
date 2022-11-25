package guess.controller;

import guess.domain.answer.ErrorDetails;
import guess.dto.result.ResultDto;
import guess.service.AnswerService;
import guess.service.LocaleService;
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
    private final LocaleService localeService;

    @Autowired
    public AnswerController(AnswerService answerService, LocaleService localeService) {
        this.answerService = answerService;
        this.localeService = localeService;
    }

    @PostMapping("/answers")
    @ResponseStatus(HttpStatus.OK)
    public void addAnswer(@RequestParam int questionIndex, @RequestParam long answerId, HttpSession httpSession) {
        answerService.setAnswer(questionIndex, answerId, httpSession);
    }

    @GetMapping("/result")
    public ResultDto getResult(HttpSession httpSession) {
        var result = answerService.getResult(httpSession);
        List<ErrorDetails> errorDetailsList = answerService.getErrorDetailsList(httpSession);
        var language = localeService.getLanguage(httpSession);

        return ResultDto.convertToDto(result, errorDetailsList, language);
    }
}
