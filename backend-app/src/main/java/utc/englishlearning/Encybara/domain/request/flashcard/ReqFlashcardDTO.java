package utc.englishlearning.Encybara.domain.request.flashcard;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqFlashcardDTO {
    private String word;
    private List<Integer> definitionIndices;
    private Long userId;
    private int partOfSpeechIndex;
}