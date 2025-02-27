package utc.englishlearning.Encybara.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.domain.response.speechToText.ResSpeechDTO;
import utc.englishlearning.Encybara.domain.response.speechToText.SpeechData;
import utc.englishlearning.Encybara.domain.response.speechToText.SpeechResponseDTO;
import utc.englishlearning.Encybara.service.GoogleSpeechService;

@RestController
@RequestMapping("/api/v1/speech")
public class GoogleSpeechController {
    private final GoogleSpeechService googleSpeechService;

    public GoogleSpeechController(GoogleSpeechService googleSpeechService) {
        this.googleSpeechService = googleSpeechService;
    }

    @PostMapping("/convert")
    public Mono<ResponseEntity<RestResponse<ResSpeechDTO>>> convertSpeechToText(@RequestParam("file") MultipartFile file) {
        return googleSpeechService.transcribe(file)
                .map(result -> {
                    RestResponse<ResSpeechDTO> response = new RestResponse<>();
                    if (result.containsKey("error")) {
                        response.setStatusCode(400);
                        response.setError((String) result.get("error"));
                        response.setMessage((String) result.get("error"));
                        response.setData(null);
                        return ResponseEntity.badRequest().body(response);
                    } else {
                        response.setStatusCode(200);
                        response.setError(null);
                        response.setMessage("Chuyển đổi âm thanh thành công");
                        response.setData(new ResSpeechDTO(
                                (String) result.get("transcript"),
                                (Double) result.get("confidence")
                        ));
                        return ResponseEntity.ok(response);
                    }
                })
                .onErrorResume(e -> {
                    RestResponse<ResSpeechDTO> errorResponse = new RestResponse<>();
                    errorResponse.setStatusCode(400);
                    errorResponse.setError("Invalid file");
                    errorResponse.setMessage("Invalid file");
                    errorResponse.setData(null);
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }
}
