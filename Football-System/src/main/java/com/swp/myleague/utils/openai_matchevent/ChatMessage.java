package com.swp.myleague.utils.openai_matchevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String role; // "user", "assistant", "system"
    private String content;
}