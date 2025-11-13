package com.swp.myleague.utils.openai_matchevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatChoice {
    private int index;
    private ChatMessage message;
}
