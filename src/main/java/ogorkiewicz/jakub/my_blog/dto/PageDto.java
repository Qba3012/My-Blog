package ogorkiewicz.jakub.my_blog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PageDto {
    int page;
    int totalPages;
    List<PostDto> posts;
}
