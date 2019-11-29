package io.github.shamrice.discapp.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HomeOlderUpdatesViewModel {

    @Getter
    @Setter
    public static class Update {
        private String date;
        private String subject;
        private String message;
    }

    private List<Update> updateList = new ArrayList<>();
}
