package ogorkiewicz.jakub.my_blog.service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PageDto;
import ogorkiewicz.jakub.my_blog.dto.PostDto;
import ogorkiewicz.jakub.my_blog.dto.PostLikeDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.ConfirmationToken;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.model.PostLike;
import ogorkiewicz.jakub.my_blog.model.PostToken;
import ogorkiewicz.jakub.my_blog.repository.CommentRepository;
import ogorkiewicz.jakub.my_blog.repository.ConfirmationTokenRepository;
import ogorkiewicz.jakub.my_blog.repository.PostImageRepository;
import ogorkiewicz.jakub.my_blog.repository.PostLikeRepository;
import ogorkiewicz.jakub.my_blog.repository.PostRepository;

@ApplicationScoped
@AllArgsConstructor(onConstructor = @__(@Inject))
public class PostService {

    private PostImageService postImageService;
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private PostLikeRepository postLikeRepository;
    private PostImageRepository postImageRepository;
    private ConfirmationTokenRepository tokenRepository;
    private MailService mailService;
    private final int pageCount = 5;

    public PageDto getPage(int pageNumber) throws MyBlogException {
        PanacheQuery<Post> postsQuery = postRepository.findConfirmed();
        int availablePosts = (int) postsQuery.count();
        int availablePages = availablePosts / pageCount + (availablePosts % pageCount == 0 ? 0 : 1);
        int firstIndex = availablePosts - pageNumber * pageCount < 0 ? 0 : availablePosts - pageNumber * pageCount;
        int lastIndex = availablePosts - pageNumber * pageCount + pageCount - 1 > availablePosts ? availablePosts - 1
                : availablePosts - pageNumber * pageCount + pageCount - 1;

        if (pageNumber > 0) {
            List<Post> posts = postsQuery.range(firstIndex, lastIndex).list();
            Collections.sort(posts, new Comparator<Post>() {
                @Override
                public int compare(Post p1, Post p2) {
                    return p1.getCreateDate().isAfter(p2.getCreateDate()) ? -1 : 1;
                }
            });
            List<Long> postsIds = posts.stream().map(p -> p.getId()).collect(Collectors.toList());
            List<PostImage> postImages = postImageRepository.findByIdList(postsIds);

            List<PostDto> postDtoList = posts.stream().map(post -> {
                PostImage postImage = postImages.stream().filter(img -> img.getId().equals(post.getId())).findFirst()
                        .orElse(null);
                return new PostDto(post, postImage);
            }).collect(Collectors.toList());

            return new PageDto(pageNumber, availablePages, postDtoList);
        } else {
            throw new MyBlogException(ErrorCode.OUT_OF_INDEX, Post.class);
        }
    }

    public void addPost(MultipartFile multipartRequest) throws MyBlogException {
        Post post = multipartRequest.getPostDto().toEntity();
        postRepository.persist(post);

        Path imagePath = postImageService.addPostImage(multipartRequest, post);

        String token = UUID.randomUUID().toString();
        tokenRepository.persist(new PostToken(token, post));

        mailService.sendPostConfirmationEmail(post, imagePath, token);
    }

    public long addLike(PostLikeDto postLikeDto) throws MyBlogException {
        PostLike postLike = postLikeRepository.findLike(postLikeDto.getEmail(), postLikeDto.getPostId());
        if (postLike != null) {
            throw new MyBlogException(ErrorCode.ALREADY_VOTED, PostLike.class);
        } else {
            postLikeRepository.persist(postLikeDto.toEntity());
            return postLikeRepository.countLikes(postLikeDto.getPostId());
        }
    }

    public void confirmPost(String token) throws MyBlogException {
        ConfirmationToken postToken = tokenRepository.findByToken(token, PostToken.class);
        if (postToken != null) {
            postRepository.confirmPost(postToken.getId());
            tokenRepository.deleteToken(token, PostToken.class);
        } else {
            throw new MyBlogException(ErrorCode.NOT_EXIST, PostToken.class);
        }
    }

}