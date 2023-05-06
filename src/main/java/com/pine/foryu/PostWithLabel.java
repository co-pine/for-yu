package com.pine.foryu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostWithLabel {
    private ResponseBody.Post post;
    private List<String> labels;
}
